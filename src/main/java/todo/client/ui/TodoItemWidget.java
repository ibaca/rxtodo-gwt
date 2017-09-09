package todo.client.ui;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.EventType.bind;
import static org.jboss.gwt.elemento.core.EventType.blur;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.dblclick;
import static org.jboss.gwt.elemento.core.EventType.keydown;
import static org.jboss.gwt.elemento.core.InputType.checkbox;
import static org.jboss.gwt.elemento.core.InputType.text;
import static todo.client.Todo.action;
import static todo.elemento.CustomEventType.dispatchCustomEvent;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLIElement;
import org.jboss.gwt.elemento.core.Key;
import todo.client.Todo.TodoItem;
import todo.elemento.ElementoHtmlPanel;

class TodoItemWidget extends ElementoHtmlPanel<HTMLLIElement> {
    public static final String ITEM = "item";
    public final HTMLInputElement toggle;
    private final HTMLElement msg;
    private final HTMLButtonElement destroy;
    private final HTMLInputElement summary;

    public TodoItemWidget(TodoItem item) {
        super(li().data(ITEM, item.id).css("completed", item.completed).asElement());
        add(div().css("view")
                .add(toggle = input(checkbox).css("toggle").checked(item.completed).asElement())
                .add(msg = label().textContent(item.text).asElement())
                .add(destroy = button().css("destroy").asElement()));
        add(summary = input(text).css("edit").asElement());

        bind(toggle, change, ev -> {
            dispatchCustomEvent(root(), action, r -> r.complete(item, toggle.checked));
        });
        bind(msg, dblclick, ev -> {
            root().classList.add("editing");
            summary.value = msg.textContent;
            summary.focus();
        });
        bind(destroy, click, ev -> {
            dispatchCustomEvent(root(), action, r -> r.remove(item));
        });
        bind(summary, keydown, ev -> {
            if (Key.Escape.match(ev)) {
                summary.value = item.text;
                root().classList.remove("editing");
            } else if (Key.Enter.match(ev)) {
                summary.blur();
            }
        });
        bind(summary, blur, ev -> {
            String value = summary.value.trim();
            if (value.length() == 0) {
                removeFromParent();
                dispatchCustomEvent(root(), action, r -> r.remove(item));
            } else {
                root().classList.remove("editing");
                msg.textContent = value;
                dispatchCustomEvent(root(), action, r -> r.rename(item, value));
            }
        });
    }
}
