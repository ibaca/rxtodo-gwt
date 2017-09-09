package todo.client;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.footer;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.header;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.label;
import static org.jboss.gwt.elemento.core.Elements.li;
import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.gwt.elemento.core.Elements.span;
import static org.jboss.gwt.elemento.core.Elements.ul;
import static org.jboss.gwt.elemento.core.EventType.change;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.gwt.elemento.core.EventType.keydown;
import static org.jboss.gwt.elemento.core.InputType.checkbox;
import static org.jboss.gwt.elemento.core.InputType.text;
import static todo.client.Main.Filter.ACTIVE;
import static todo.client.Main.Filter.ALL;
import static todo.client.Main.Filter.COMPLETED;
import static todo.client.Main.i18n;
import static todo.client.Main.msg;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLUListElement;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventType;
import org.jboss.gwt.elemento.core.Key;
import todo.client.Main.Filter;
import todo.client.Main.Repository;
import todo.client.Main.TodoItem;

class ApplicationElement extends ElementoHtmlPanel<HTMLElement> {
    private final HTMLInputElement newTodo;
    private final HTMLElement main;
    private final HTMLInputElement toggleAll;
    private final ElementoListPanel<HTMLUListElement, TodoItemElement> list;
    private final HTMLElement footer;
    private final HTMLElement count;
    private final HTMLElement filterAll;
    private final HTMLElement filterActive;
    private final HTMLElement filterCompleted;
    private final HTMLButtonElement clearCompleted;

    ApplicationElement(Repository repository) {
        super(section().css("todoapp").asElement());
        add(header().css("header")
                .add(h(1).textContent(i18n.todos()))
                .add(newTodo = input(text).css("new-todo").apply(el -> {
                    el.placeholder = i18n.new_todo();
                    el.autofocus = true;
                }).asElement()));
        add(main = section().css("main")
                .add(toggleAll = input(checkbox).css("toggle-all").id().asElement())
                .add(label().apply(el -> el.htmlFor = toggleAll.id).textContent(i18n.complete_all()))
                .apply(addWidget(list = ElementoListPanel.of(ul().css("todo-list"))))
                .asElement());
        add(footer = footer().css("footer")
                .add(count = span().css("todo-count").innerHtml(msg.items(0)).asElement())
                .add(ul().css("filters")
                        .add(filterAll = filter(ALL, i18n.filter_all()))
                        .add(filterActive = filter(ACTIVE, i18n.filter_active()))
                        .add(filterCompleted = filter(COMPLETED, i18n.filter_completed())))
                .add(clearCompleted = button()
                        .css("clear-completed")
                        .textContent(i18n.clear_completed())
                        .asElement())
                .asElement());

        EventType.bind(newTodo, keydown, ev -> {
            if (Key.Enter.match(ev)) {
                String text = newTodo.value.trim();
                if (text.length() != 0) {
                    TodoItem item = repository.add(text);
                    list.add(new TodoItemElement(this, repository, item));
                    newTodo.value = "";
                    update();
                }
            }
        });

        EventType.bind(toggleAll, change, event -> {
            for (TodoItemElement li : list.children()) {
                li.asElement().classList.toggle("completed", toggleAll.checked);
                li.toggle.checked = toggleAll.checked;
            }
            repository.completeAll(toggleAll.checked);
            update();
        });

        EventType.bind(clearCompleted, click, event -> {
            Set<String> ids = new HashSet<>();
            for (TodoItemElement li : list.children()) {
                if (li.asElement().classList.contains("completed")) {
                    String id = String.valueOf(li.asElement().dataset.get(TodoItemElement.ITEM));
                    if (id != null) ids.add(id);
                    li.removeFromParent();
                }
            }
            repository.removeAll(ids);
            update();
        });

        Consumer<Repository> onChange = n -> {
            list.clear();
            for (TodoItem item : repository.items()) {
                list.add(new TodoItemElement(this, repository, item));
            }
            update();
        };
        repository.onExternalModification(onChange);
        onChange.accept(repository);
    }

    private Filter filter;

    void filter(Filter filter) {
        this.filter = filter;
        filterAll.classList.toggle("selected", filter == ALL);
        filterActive.classList.toggle("selected", filter == ACTIVE);
        filterCompleted.classList.toggle("selected", filter == COMPLETED);
        update();
    }

    void update() {
        int activeCount = 0;
        int completedCount = 0;
        int size = list.size();

        Elements.setVisible(main, size > 0);
        Elements.setVisible(footer, size > 0);
        for (TodoItemElement li : list.children()) {
            if (li.asElement().classList.contains("completed")) {
                completedCount++;
                li.setVisible(filter != ACTIVE);
            } else {
                li.setVisible(filter != COMPLETED);
                activeCount++;
            }
        }
        toggleAll.checked = (size == completedCount);
        Elements.innerHtml(count, msg.items(activeCount));
        Elements.setVisible(clearCompleted, completedCount != 0);
    }

    static HTMLElement filter(Filter f, String text) {
        return li().add(a(f.fragment()).textContent(text)).asElement();
    }
}
