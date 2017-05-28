package todo.client;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.div;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.EventType.blur;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.dblclick;
import static org.jboss.gwt.elemento.core.EventType.keydown;
import static org.jboss.gwt.elemento.core.InputType.checkbox;
import static org.jboss.gwt.elemento.core.InputType.text;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;
import org.jboss.gwt.elemento.core.IsElement;

class TodoItemElement implements IsElement {

    private final Main.TodoItem item;
    private final ApplicationElement application;
    private final Main.Repository repository;

    private final HTMLElement container;
    private final HTMLInputElement toggle;
    private final HTMLElement label;
    private final HTMLInputElement summary;

    private boolean escape;

    TodoItemElement(ApplicationElement application, Main.Repository repository, Main.TodoItem item) {
        this.application = application;
        this.repository = repository;
        this.item = item;
        this.container = li().data("item", item.id)
                .add(div().css("view")
                        .add(toggle = input(checkbox).on(change, ev -> toggle()).css("toggle").asElement())
                        .add(label = h("label").on(dblclick, ev -> edit()).textContent(item.text).asElement())
                        .add(button().on(click, ev -> destroy()).css("destroy")))
                .add(summary = input(text).on(keydown, this::keyDown).on(blur, ev -> blur()).css("edit").asElement())
                .asElement();
        this.container.classList.toggle("completed", item.completed);
        this.toggle.checked = item.completed;
    }

    @Override
    public HTMLElement asElement() {
        return container;
    }

    // ------------------------------------------------------ event handler

    private void toggle() {
        container.classList.toggle("completed", toggle.checked);
        repository.complete(item, toggle.checked);
        application.update();
    }

    private void edit() {
        escape = false;
        container.classList.add("editing");
        summary.value = label.textContent;
        summary.focus();
    }

    private void destroy() {
        container.parentNode.removeChild(container);
        repository.remove(item);
        application.update();
    }

    private void keyDown(Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        if ("Escape".equals(keyboardEvent.code)) {
            escape = true;
            container.classList.remove("editing");

        } else if ("Enter".equals(keyboardEvent.key)) {
            blur();
        }
    }

    private void blur() {
        String value = summary.value.trim();
        if (value.length() == 0) {
            destroy();
        } else {
            container.classList.remove("editing");
            if (!escape) {
                label.textContent = value;
                repository.rename(item, value);
            }
        }
    }
}
