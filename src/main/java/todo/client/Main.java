package todo.client;

import static elemental2.dom.DomGlobal.window;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.intendia.rxgwt.elemento.RxElemento;
import elemental2.core.Global;
import elemental2.dom.DomGlobal;
import elemental2.webstorage.Storage;
import elemental2.webstorage.WebStorageWindow;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import org.jboss.gwt.elemento.core.EventType;
import rx.Observable;

public class Main implements EntryPoint {
    public static final TodoConstants i18n = GWT.create(TodoConstants.class);
    public static final TodoMessages msg = GWT.create(TodoMessages.class);

    @Override
    public void onModuleLoad() {
        Repository repository = new Repository();

        ApplicationElement application = new ApplicationElement(repository);
        RootPanel.get().add(application);

        FooterElement footer = new FooterElement();
        RootPanel.get().add(footer);

        History.addValueChangeHandler(event -> application.filter(Filter.parseToken(event.getValue())));
        History.fireCurrentHistoryState();
    }

    public interface TodoConstants extends Constants {
        @DefaultStringValue("Clear completed") String clear_completed();
        @DefaultStringValue("Mark all as complete") String complete_all();
        @DefaultStringValue("Created by") String created_by();
        @DefaultStringValue("Double-click to edit a todo") String double_click_to_edit();
        @DefaultStringValue("Active") String filter_active();
        @DefaultStringValue("All") String filter_all();
        @DefaultStringValue("Completed") String filter_completed();
        @DefaultStringValue("What needs to be done?") String new_todo();
        @DefaultStringValue("Part of") String part_of();
        @DefaultStringValue("todos") String todos();
    }

    public interface TodoMessages extends Messages {
        @DefaultMessage("<strong>{0, number}</strong> items left")
        @AlternateMessage({ "one", "<strong>{0, number}</strong> item left" })
        SafeHtml items(@PluralCount int items);
    }

    public static class Repository {
        private static final String DEFAULT_KEY = "todos-elemento";
        private final Storage storage = WebStorageWindow.of(DomGlobal.window).localStorage;
        private final JsPropertyMap<TodoItem> items = Optional.ofNullable(storage.getItem(DEFAULT_KEY))
                .map(json -> Js.<JsPropertyMap<TodoItem>>cast(Global.JSON.parse(json)))
                .orElse(Js.cast(JsPropertyMap.of()));

        public TodoItem add(String text) {
            TodoItem item = new TodoItem();
            item.id = uuid();
            item.text = text;
            item.completed = false;
            items.set(item.id, item);

            save();
            return item;
        }

        public void completeAll(boolean completed) {
            for (TodoItem i : items()) { i.completed = completed; } save();
        }

        public void complete(TodoItem item, boolean completed) {
            items.get(item.id).completed = completed; save();
        }

        public void rename(TodoItem item, String text) {
            items.get(item.id).text = text; save();
        }

        public void remove(TodoItem item) {
            items.delete(item.id); save();
        }

        public void removeAll(Set<String> ids) {
            for (String id : ids) items.delete(id); save();
        }

        private void save() {
            storage.setItem(DEFAULT_KEY, Global.JSON.stringify(items));
        }

        public Collection<TodoItem> items() {
            List<TodoItem> copy = new ArrayList<>();
            items.forEach(i -> copy.add(items.get(i)));
            return copy;
        }

        public Observable<Repository> onExternalModification() {
            return RxElemento.fromEvent(window, EventType.storage)
                    .filter(ev -> DEFAULT_KEY.equals(ev.key)).map(ev -> this);
        }
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static class TodoItem {
        public String id;
        public String text;
        public boolean completed;
    }

    public enum Filter {
        ALL("#/"), ACTIVE("#/active"), COMPLETED("#/completed");
        private final String fragment;
        Filter(final String fragment) { this.fragment = fragment; }
        public String fragment() { return fragment; }
        public String filter() { return name().toLowerCase(); }
        public static Filter parseToken(final String token) {
            switch (token == null ? "" : token) {
                case "/active": return ACTIVE;
                case "/completed": return COMPLETED;
                default: return ALL;
            }
        }
    }

    public static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Generate a RFC4122, version 4 ID. Example: "92329D39-6F5C-4520-ABFC-AAB64544E172" */
    public static String uuid() {
        char[] uuid = new char[36];

        // rfc4122 requires these characters
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'; uuid[14] = '4';

        // Fill in random data.  At i==19 set the high bits of clock sequence as per rfc4122, sec. 4.1.5
        for (int i = 0, r; i < 36; i++) {
            if (uuid[i] == 0) {
                r = (int) (Math.random() * 16);
                uuid[i] = CHARS[(i == 19) ? (r & 0x3) | 0x8 : r & 0xf];
            }
        }
        return new String(uuid);
    }
}
