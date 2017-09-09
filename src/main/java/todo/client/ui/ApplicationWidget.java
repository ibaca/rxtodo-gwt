package todo.client.ui;

import static com.intendia.rxgwt.elemento.RxElemento.fromEvent;
import static java.util.stream.Collectors.toSet;
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
import static org.jboss.gwt.elemento.core.Key.Enter;
import static todo.elemento.CustomEventType.dispatchCustomEvent;
import static todo.client.Todo.action;
import static todo.client.Todo.Filter.ACTIVE;
import static todo.client.Todo.Filter.ALL;
import static todo.client.Todo.Filter.COMPLETED;
import static todo.client.Todo.i18n;
import static todo.client.Todo.msg;

import com.intendia.rxgwt.user.RxWidget;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLUListElement;
import org.jboss.gwt.elemento.core.Elements;
import todo.client.Todo.Filter;
import todo.client.Repository;
import todo.elemento.ElementoHtmlPanel;
import todo.elemento.ElementoListPanel;

public class ApplicationWidget extends ElementoHtmlPanel<HTMLElement> {
    final HTMLInputElement newTodo;
    private final HTMLElement main;
    final HTMLInputElement toggleAll;
    private final ElementoListPanel<HTMLUListElement, TodoItemWidget> list;
    private final HTMLElement footer;
    private final HTMLElement count;
    private final HTMLElement filterAll;
    private final HTMLElement filterActive;
    private final HTMLElement filterCompleted;
    final HTMLButtonElement clearCompleted;

    public ApplicationWidget() {
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

        RxWidget rx = new RxWidget(this); // <-- this handle un/subscription on attach, and re-subscribe on failures
        rx.registerAttachObservable(fromEvent(newTodo, keydown).filter(Enter::match).doOnNext(ev -> {
            String text = newTodo.value.trim();
            newTodo.value = "";
            if (!text.isEmpty()) dispatchCustomEvent(root(), action, r -> r.add(text));
        }));

        rx.registerAttachObservable(fromEvent(toggleAll, change).doOnNext(ev -> {
            dispatchCustomEvent(root(), action, r -> r.completeAll(toggleAll.checked));
        }));

        rx.registerAttachObservable(fromEvent(clearCompleted, click).doOnNext(ev -> {
            dispatchCustomEvent(root(), action, r -> {
                r.removeAll(r.items().stream().filter(i -> i.completed).map(i -> i.id).collect(toSet()));
            });
        }));
    }

    public void draw(Repository repository) {
        list.clear();
        // TODO removing  widgets and creating back is slow! fix with something like D3.selectAll for Widgets
        repository.items().forEach(item -> list.add(new TodoItemWidget(item)));
        update();
    }

    private Filter filter;

    public void filter(Filter filter) {
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
        for (TodoItemWidget li : list.children()) {
            if (li.root().classList.contains("completed")) {
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
