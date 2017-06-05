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
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Key;
import todo.client.Main.Repository;
import todo.client.Main.TodoItem;

class TodoItemElement implements IsElement {
    public static final String ITEM = "item";
    private final HTMLElement root;
    private final HTMLInputElement toggle;
    private final HTMLElement msg;
    private final HTMLButtonElement destroy;
    private final HTMLInputElement summary;

    private boolean escape;

    TodoItemElement(ApplicationElement application, Repository repository, TodoItem item) {
        root = li().data(ITEM, item.id).css("completed", item.completed)
                .add(div().css("view")
                        .add(toggle = input(checkbox).css("toggle").checked(item.completed).asElement())
                        .add(msg = label().textContent(item.text).asElement())
                        .add(destroy = button().css("destroy").asElement()))
                .add(summary = input(text).css("edit").asElement())
                .asElement();

        bind(toggle, change, ev -> {
            root.classList.toggle("completed", toggle.checked);
            repository.complete(item, toggle.checked);
            application.update();
        });
        bind(msg, dblclick, ev -> {
            escape = false;
            root.classList.add("editing");
            summary.value = msg.textContent;
            summary.focus();
        });
        bind(destroy, click, ev -> {
            root.parentNode.removeChild(root);
            repository.remove(item);
            application.update();
        });
        Runnable doBlur = () -> {
            String value = summary.value.trim();
            if (value.length() == 0) {
                root.parentNode.removeChild(root);
                repository.remove(item);
                application.update();
            } else {
                root.classList.remove("editing");
                if (!escape) {
                    msg.textContent = value;
                    repository.rename(item, value);
                }
            }
        };
        bind(summary, keydown, ev -> {
            if (Key.Escape.match(ev)) {
                escape = true;
                root.classList.remove("editing");
            } else if (Key.Enter.match(ev)) {
                doBlur.run();
            }
        });
        bind(summary, blur, ev -> doBlur.run());
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }
}
