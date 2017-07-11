package todo.client;

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

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLIElement;
import org.jboss.gwt.elemento.core.Key;
import todo.client.Main.Repository;
import todo.client.Main.TodoItem;

class TodoItemElement extends ElementoHtmlPanel<HTMLLIElement> {
    public static final String ITEM = "item";
    public final HTMLInputElement toggle;
    private final HTMLElement msg;
    private final HTMLButtonElement destroy;
    private final HTMLInputElement summary;

    private boolean escape;

    TodoItemElement(ApplicationElement application, Repository repository, TodoItem item) {
        super(li().data(ITEM, item.id).css("completed", item.completed).asElement());
        add(div().css("view")
                .add(toggle = input(checkbox).css("toggle").checked(item.completed).asElement())
                .add(msg = label().textContent(item.text).asElement())
                .add(destroy = button().css("destroy").asElement()));
        add(summary = input(text).css("edit").asElement());

        bind(toggle, change, ev -> {
            asElement().classList.toggle("completed", toggle.checked);
            repository.complete(item, toggle.checked);
            application.update();
        });
        bind(msg, dblclick, ev -> {
            escape = false;
            asElement().classList.add("editing");
            summary.value = msg.textContent;
            summary.focus();
        });
        bind(destroy, click, ev -> {
            removeFromParent();
            repository.remove(item);
            application.update();
        });
        Runnable doBlur = () -> {
            String value = summary.value.trim();
            if (value.length() == 0) {
                removeFromParent();
                repository.remove(item);
                application.update();
            } else {
                asElement().classList.remove("editing");
                if (!escape) {
                    msg.textContent = value;
                    repository.rename(item, value);
                }
            }
        };
        bind(summary, keydown, ev -> {
            if (Key.Escape.match(ev)) {
                escape = true;
                asElement().classList.remove("editing");
            } else if (Key.Enter.match(ev)) {
                doBlur.run();
            }
        });
        bind(summary, blur, ev -> doBlur.run());
    }
}
