package todo.elemento;

import com.google.web.bindery.event.shared.HandlerRegistration;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.EventTarget;
import java.util.function.Consumer;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.jboss.gwt.elemento.core.EventType;
import org.jboss.gwt.elemento.core.IsElement;

// TODO add a factory including CustomEventInit options? I think the options are frequently managed by type, so allow
// to place this info in the even spec is a good practice (if it is actually per-type!?).
public class CustomEventType<V extends EventTarget, D> extends EventType<CustomEvent, V> {

    public CustomEventType(String name) { super(name); }

    // TODO use EventCallbackFn? this will required to remove the 'E extends Event' restriction
    public static <D> HandlerRegistration bindDetail(EventTarget target, CustomEventType<?, D> type, Consumer<D> fn) {
        return EventType.bind(target, type.getName(), e -> fn.accept(detail(Js.cast(e), type)));
    }

    public static <D> D detail(CustomEvent ev, @SuppressWarnings("unused") CustomEventType<?, ? extends D> type) {
        return Js.cast(ev.detail);
    }

    public static <D> CustomEvent createCustomEvent(CustomEventType<?, D> type, D detail) {
        CustomEventInit init = Js.cast(JsPropertyMap.of());
        init.setDetail(detail); init.setBubbles(true);
        return Js.cast(new CustomEvent(type.getName(), init));
    }

    public static <D> void dispatchCustomEvent(IsElement<?> target, CustomEventType<?, D> type, D detail) {
        dispatchCustomEvent(target.asElement(), type, detail);
    }

    public static <D> boolean dispatchCustomEvent(EventTarget eventTarget, CustomEventType<?, D> type, D detail) {
        return eventTarget.dispatchEvent(createCustomEvent(type, detail));
    }
}
