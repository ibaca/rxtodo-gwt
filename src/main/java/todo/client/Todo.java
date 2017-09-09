package todo.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import elemental2.dom.DomGlobal;
import elemental2.dom.EventTarget;
import java.util.function.Consumer;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import todo.client.ui.ApplicationWidget;
import todo.client.ui.FooterWidget;
import todo.elemento.CustomEventType;

public class Todo implements EntryPoint {
    public static final TodoConstants i18n = GWT.create(TodoConstants.class);
    public static final TodoMessages msg = GWT.create(TodoMessages.class);

    //@formatter:off Application events
    public static final CustomEventType<EventTarget, Consumer<Repository>> action = new CustomEventType<>("todo.action");
    //@formatter:on

    @Override
    public void onModuleLoad() {
        Repository repository = new Repository();

        ApplicationWidget application = new ApplicationWidget();
        RootPanel.get().add(application);

        FooterWidget footer = new FooterWidget();
        RootPanel.get().add(footer);

        repository.changes().subscribe(n -> application.draw(repository));
        History.addValueChangeHandler(ev -> application.filter(Filter.parseToken(ev.getValue())));
        History.fireCurrentHistoryState();

        CustomEventType.bindDetail(DomGlobal.document, action, ev -> {
            GWT.log("processing action…");
            ev.accept(repository);
        });
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

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static class TodoItem {
        public String id;
        public String text;
        public boolean completed;
        public static @JsOverlay TodoItem create(String text) {
            TodoItem i = new TodoItem(); i.id = uuid(); i.text = text; i.completed = false; return i;
        }
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

    public static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    /** Generate a RFC4122, version 4 ID. Example: "92329D39-6F5C-4520-ABFC-AAB64544E172" */
    public static String uuid() {
        char[] uuid = new char[36];

        // rfc4122 requires these characters
        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'; uuid[14] = '4';

        // Fill in random data.  At i==19 set the high bits of clock sequence as per rfc4122, sec. 4.1.5
        for (int i = 0, r; i < 36; i++) {
            if (uuid[i] == 0) {
                r = (int) (Math.random() * 16);
                uuid[i] = CHARS.charAt((i == 19) ? (r & 0x3) | 0x8 : r & 0xf);
            }
        }
        return new String(uuid);
    }
}
