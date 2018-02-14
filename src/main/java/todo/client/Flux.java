package todo.client;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Flux allows multiples stores but should have only one dispatcher. If more than one store depends in the same action
 * you can use Dispatcher.waitFor to be sure the store X is updated before the store Y evaluation.
 *
 * WaitFor is not required in Redux bc there are one and only one store for the whole application, so no singleton
 * dispatcher is required (actually the dispatcher is hidden inside the store, so "no dispatcher").
 */
public class Flux {
    public static class Type<T extends Action> {
        public static <T extends Action> Type<T> of(String name) {
            Type<T> out = new Type<>(); out.name = name; return out;
        }
        public String name;
    }

    public static class Action<A extends Action<A>> {
        public Type<A> type;
    }

    private static final Dispatcher DISPATCHER = new Dispatcher();
    public static Dispatcher dispatcher() { return DISPATCHER; }

    public static class Dispatcher {
        private final Map<Integer, Consumer<Action>> callbacks;
        private final AtomicBoolean dispatching;
        private final Map<Integer, Boolean> handled;
        private final Map<Integer, Boolean> pending;
        private final AtomicInteger lastID;
        private @Nullable Action pendingPayload;

        public Dispatcher() {
            this.callbacks = new HashMap<>();
            this.dispatching = new AtomicBoolean(false);
            this.handled = new HashMap<>();
            this.pending = new HashMap<>();
            this.lastID = new AtomicInteger(1);
        }

        /**
         * Registers a callback to be invoked with every dispatched payload.
         * Returns a token that can be used with `waitFor()`.
         */
        public int register(Consumer<Action> fn) {
            int id = lastID.getAndIncrement();
            callbacks.put(id, fn);
            return id;
        }

        /** Removes a callback based on its token. */
        public void unregister(int id) {
            checkState(callbacks.containsKey(id), "`%s` does not map to a registered callback.", id);
            callbacks.remove(id);
        }

        /**
         * Waits for the callbacks specified to be invoked before continuing execution
         * of the current callback. This method should only be used by a callback in
         * response to a dispatched payload.
         */
        public void waitFor(int... ids) {
            checkState(dispatching.get(), "Must be invoked while dispatching.");
            for (int id : ids) {
                if (pending.get(id)) {
                    checkState((boolean) handled.get(id), "Circular dependency detected while waiting for `%s`.", id);
                    continue;
                }
                checkState(callbacks.containsKey(id), "`%s` does not map to a registered callback.", id);
                invokeCallback(id);
            }
        }

        /** Dispatches a payload to all registered callbacks. */
        public void dispatch(Action payload) {
            checkState(!dispatching.get(), "Cannot dispatch in the middle of a dispatch.");
            startDispatching(payload);
            try {
                for (int id : callbacks.keySet()) {
                    if (pending.get(id)) continue;
                    invokeCallback(id);
                }
            } finally {
                stopDispatching();
            }
        }

        /** Is this Dispatcher currently dispatching. */
        public boolean isDispatching() {
            return dispatching.get();
        }

        /** Call the callback stored with the given id. Also do some internal bookkeeping. */
        private void invokeCallback(int id) {
            pending.put(id, true);
            callbacks.get(id).accept(pendingPayload);
            handled.put(id, true);
        }

        /** Set up bookkeeping needed when dispatching. */
        private void startDispatching(Action payload) {
            for (int id : callbacks.keySet()) {
                pending.put(id, false);
                handled.put(id, false);
            }
            pendingPayload = payload;
            dispatching.set(true);
        }

        /** Clear bookkeeping used for dispatching. */
        private void stopDispatching() {
            pendingPayload = null;
            dispatching.set(false); ;
        }
    }

    /**
     * This class represents the most basic functionality for a FluxStore. Do not extend this store directly;
     * instead extend FluxReduceStore when creating a new store.
     */
    public static class FluxStore<TState> {
        private final Dispatcher dispatcher;
        private final int dispatchToken;
        private final BiFunction<TState, Action, TState> reducer;
        protected final PublishSubject<TState> emitter;
        protected boolean changed;
        protected TState state;

        public FluxStore(Supplier<TState> initialState, BiFunction<TState, Action, TState> reducer) {
            this(dispatcher(), initialState, reducer);
        }

        public FluxStore(Dispatcher dispatcher, Supplier<TState> initialState,
                BiFunction<TState, Action, TState> reducer) {
            this.dispatcher = dispatcher;
            this.dispatchToken = dispatcher.register(this::invokeOnDispatch);
            this.reducer = reducer;
            this.emitter = PublishSubject.create();
            this.changed = false;
            this.state = initialState.get();
        }

        /** Used to reduce a stream of actions coming from the dispatcher into a single state object. */
        public TState reduce(TState state, Action action) { return reducer.apply(state, action); }

        /**
         * Getter that exposes the entire state of this store. If your state is not
         * immutable you should override this and not expose _state directly.
         */
        public TState getState() { return state; }

        public Dispatcher getDispatcher() { return dispatcher; }

        public Observable<TState> changes() { return emitter; }

        /**
         * This exposes a unique string to identify each store's registered callback. This is used with the
         * dispatcher's waitFor method to declaratively depend on other stores updating themselves first.
         */
        public int getDispatchToken() { return dispatchToken; }

        /** Returns whether the store has changed during the most recent dispatch. */
        public boolean hasChanged() {
            checkState(dispatcher.isDispatching(), "Must be invoked while dispatching.");
            return changed;
        }

        protected void emitChange() {
            checkState(dispatcher.isDispatching(), "Must be invoked while dispatching.");
            changed = true;
        }

        /**
         * This method encapsulates all logic for invoking __onDispatch. It should be used for things like catching
         * changes and emitting them after the subclass has handled a payload.
         */
        protected void invokeOnDispatch(Action payload) {
            changed = false;

            // Reduce the stream of incoming actions to state, update when necessary.
            TState startingState = state;
            TState endingState = reduce(startingState, payload);

            // This means your ending state should never be undefined.
            checkState(endingState != null, "%s returned undefined from reduce(...), did you forget to return " +
                    "state in the default case? (use null if this was intentional)", getClass().getName());

            if (startingState != endingState/*only works with immutable states, on purpose!*/) {
                state = endingState;

                // `emitChange()` sets `changed` to true and then the actual change will be fired from the emitter
                // at the end of the dispatch, this is required in order to support methods like `hasChanged()`
                emitChange();
            }

            if (changed) emitter.onNext(state);
        }
    }
}
