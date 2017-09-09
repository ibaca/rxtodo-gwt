package todo.client;

import static com.intendia.rxgwt.elemento.RxElemento.fromEvent;
import static elemental2.core.Global.JSON;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.gwt.elemento.core.EventType.storage;

import com.google.gwt.core.client.GWT;
import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import rx.Observable;
import rx.subjects.PublishSubject;
import todo.client.Todo.TodoItem;

public class Repository {
    private static final String DEFAULT_KEY = "rxtodo";
    private final Storage store;
    private final Observable<Collection<TodoItem>> changes;
    private final PublishSubject<JsPropertyMap<TodoItem>> localChanges;
    private JsPropertyMap<TodoItem> items;

    public Repository() {
        store = WebStorageWindow.of(window).localStorage;
        localChanges = PublishSubject.create();
        changes = fromEvent(window, storage)
                .filter(ev -> DEFAULT_KEY.equals(ev.key)).map(e1 -> e1.newValue)
                .startWith(store.getItem(DEFAULT_KEY)) // initial load
                .map(str -> items = Optional.ofNullable(str)
                        .map(json -> {
                            if (!json.isEmpty()) try {
                                return Js.<JsPropertyMap<TodoItem>>cast(JSON.parse(json));
                            } catch (Exception e1) {
                                GWT.log("ups… error parsing stored todos", e1);
                            }
                            return null;
                        })
                        .orElse(Js.cast(JsPropertyMap.of())))
                .mergeWith(localChanges) // local changes
                .<Collection<TodoItem>>map(e -> {
                    List<TodoItem> copy = new ArrayList<>();
                    e.forEach(i -> copy.add(e.get(i)));
                    return copy;
                })
                .replay(1).autoConnect(-1);
    }

    public TodoItem add(String text) {
        TodoItem item = TodoItem.create(text);
        items.set(item.id, item);
        save();
        return item;
    }

    public void completeAll(boolean completed) {
        for (TodoItem i : items()) { i.completed = completed; }
        save();
    }

    public void complete(TodoItem item, boolean completed) {
        items.get(item.id).completed = completed;
        save();
    }

    public void rename(TodoItem item, String text) {
        items.get(item.id).text = text;
        save();
    }

    public void remove(TodoItem item) {
        items.delete(item.id);
        save();
    }

    public void removeAll(Set<String> ids) {
        for (String id : ids) items.delete(id);
        save();
    }

    private void save() {
        store.setItem(DEFAULT_KEY, JSON.stringify(items));
        localChanges.onNext(items);
    }

    public Collection<TodoItem> items() {
        List<TodoItem> copy = new ArrayList<>();
        items.forEach(i -> copy.add(items.get(i)));
        return copy;
    }

    public Observable<Collection<TodoItem>> changes() { return changes; }
}
