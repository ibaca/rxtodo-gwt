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

class TodoItemElement implements IsElement {
    private final HTMLElement container;
    private final HTMLInputElement toggle;
    private final HTMLElement msg;
    private final HTMLButtonElement destroy;
    private final HTMLInputElement summary;

    private boolean escape;

    TodoItemElement(ApplicationElement application, Main.Repository repository, Main.TodoItem item) {
        this.container = li().data("item", item.id)
                .add(div().css("view")
                        .add(toggle = input(checkbox).css("toggle").asElement())
                        .add(msg = label().textContent(item.text).asElement())
                        .add(destroy = button().css("destroy").asElement()))
                .add(summary = input(text).css("edit").asElement())
                .asElement();
        this.container.classList.toggle("completed", item.completed);
        this.toggle.checked = item.completed;

        bind(toggle, change, ev -> {
            container.classList.toggle("completed", toggle.checked);
            repository.complete(item, toggle.checked);
            application.update();
        });
        bind(msg, dblclick, ev -> {
            escape = false;
            container.classList.add("editing");
            summary.value = msg.textContent;
            summary.focus();
        });
        bind(destroy, click, ev -> {
            container.parentNode.removeChild(container);
            repository.remove(item);
            application.update();
        });
        Runnable doBlur = () -> {
            String value = summary.value.trim();
            if (value.length() == 0) {
                container.parentNode.removeChild(container);
                repository.remove(item);
                application.update();
            } else {
                container.classList.remove("editing");
                if (!escape) {
                    msg.textContent = value;
                    repository.rename(item, value);
                }
            }
        };
        bind(summary, keydown, event -> {
            if ("Escape".equals(event.code)) {
                escape = true;
                container.classList.remove("editing");
            } else if ("Enter".equals(event.key)) {
                doBlur.run();
            }
        });
        bind(summary, blur, ev -> doBlur.run());
    }

    @Override
    public HTMLElement asElement() {
        return container;
    }
}
