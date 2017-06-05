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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventType;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.gwt.elemento.core.Key;
import todo.client.Main.Filter;
import todo.client.Main.Repository;
import todo.client.Main.TodoItem;

class ApplicationElement implements IsElement {
    private final HTMLElement root;
    private final HTMLInputElement newTodo;
    private final HTMLElement main;
    private final HTMLInputElement toggleAll;
    private final HTMLElement list;
    private final HTMLElement footer;
    private final HTMLElement count;
    private final HTMLElement filterAll;
    private final HTMLElement filterActive;
    private final HTMLElement filterCompleted;
    private final HTMLButtonElement clearCompleted;

    ApplicationElement(Repository repository) {
        root = section().css("todoapp")
                .add(header().css("header")
                        .add(h(1).textContent(i18n.todos()))
                        .add(newTodo = input(text).css("new-todo").apply(el -> {
                            el.placeholder = i18n.new_todo();
                            el.autofocus = true;
                        }).asElement()))
                .add(main = section().css("main")
                        .add(toggleAll = input(checkbox).css("toggle-all").id().asElement())
                        .add(label().apply(el -> el.htmlFor = toggleAll.id).textContent(i18n.complete_all()))
                        .add(list = ul().css("todo-list").asElement())
                        .asElement())
                .add(footer = footer().css("footer")
                        .add(count = span().css("todo-count").innerHtml(msg.items(0)).asElement())
                        .add(ul().css("filters")
                                .add(filterAll = filter(ALL, i18n.filter_all()))
                                .add(filterActive = filter(ACTIVE, i18n.filter_active()))
                                .add(filterCompleted = filter(COMPLETED, i18n.filter_completed())))
                        .add(clearCompleted = button()
                                .css("clear-completed")
                                .textContent(i18n.clear_completed())
                                .asElement())
                        .asElement())
                .asElement();

        EventType.bind(newTodo, keydown, ev -> {
            if (Key.Enter.match(ev)) {
                String text = newTodo.value.trim();
                if (text.length() != 0) {
                    TodoItem item = repository.add(text);
                    list.appendChild(new TodoItemElement(this, repository, item).asElement());
                    newTodo.value = "";
                    update();
                }
            }
        });

        EventType.bind(toggleAll, change, event -> {
            for (HTMLElement li : Elements.children(list)) {
                li.classList.toggle("completed", toggleAll.checked);
                ((HTMLInputElement) li.firstElementChild.firstElementChild).checked = toggleAll.checked;
            }
            repository.completeAll(toggleAll.checked);
            update();
        });

        EventType.bind(clearCompleted, click, event -> {
            Set<String> ids = new HashSet<>();
            for (Iterator<HTMLElement> iterator = Elements.iterator(list); iterator.hasNext(); ) {
                HTMLElement li = iterator.next();
                if (li.classList.contains("completed")) {
                    String id = String.valueOf(li.dataset.get(TodoItemElement.ITEM));
                    if (id != null) ids.add(id);
                    iterator.remove();
                }
            }
            repository.removeAll(ids);
            update();
        });

        repository.onExternalModification().startWith(repository).subscribe(n -> {
            Elements.removeChildrenFrom(list);
            for (TodoItem item : repository.items()) {
                list.appendChild(new TodoItemElement(this, repository, item).asElement());
            }
            update();
        });
    }

    static HTMLElement filter(Filter f, String text) {
        return li().add(a(f.fragment()).textContent(text)).asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
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
        int size = (int) list.childElementCount;

        Elements.setVisible(main, size > 0);
        Elements.setVisible(footer, size > 0);
        for (HTMLElement li : Elements.children(list)) {
            if (li.classList.contains("completed")) {
                completedCount++;
                Elements.setVisible(li, filter != ACTIVE);
            } else {
                Elements.setVisible(li, filter != COMPLETED);
                activeCount++;
            }
        }
        toggleAll.checked = (size == completedCount);
        Elements.innerHtml(count, msg.items(activeCount));
        Elements.setVisible(clearCompleted, completedCount != 0);
    }
}
