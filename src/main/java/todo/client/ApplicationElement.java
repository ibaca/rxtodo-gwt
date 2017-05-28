package todo.client;

import static org.jboss.gwt.elemento.core.Elements.button;
import static org.jboss.gwt.elemento.core.Elements.h;
import static org.jboss.gwt.elemento.core.Elements.input;
import static org.jboss.gwt.elemento.core.Elements.li;
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

import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.KeyboardEvent;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.EventType;
import org.jboss.gwt.elemento.core.IsElement;
import todo.client.Main.Filter;

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

    private final Main.Repository repository;
    private Filter filter;

    static HTMLElement filter(Filter f, String text) {
        return li().add(h("a").attr("href", f.fragment()).textContent(text)).asElement();
    }

    ApplicationElement(Main.Repository repository) {
        this.repository = repository;

        this.root = h("section").css("todoapp")
                .add(h("header").css("header")
                        .add(h("h1").textContent(i18n.todos()))
                        .add(newTodo = input(text).css("new-todo").apply(el -> {
                            el.placeholder = i18n.new_todo();
                            el.autofocus = true;
                        }).asElement()))
                .add(main = h("section").css("main")
                        .add(toggleAll = input(checkbox).css("toggle-all").id("toggle-all").asElement())
                        .add(h("label").attr("for", "toggle-all").textContent(i18n.complete_all()))
                        .add(list = h("ul").css("todo-list").asElement())
                        .asElement())
                .add(footer = h("footer").css("footer")
                        .add(count = h("span").css("todo-count").innerHtml(msg.items(0)).asElement())
                        .add(h("ul").css("filters")
                                .add(filterAll = filter(ALL, i18n.filter_all()))
                                .add(filterActive = filter(ACTIVE, i18n.filter_active()))
                                .add(filterCompleted = filter(COMPLETED, i18n.filter_completed())))
                        .add(clearCompleted = h(button)
                                .css("clear-completed")
                                .textContent(i18n.clear_completed())
                                .asElement())
                        .asElement())
                .asElement();

        EventType.bind(newTodo, keydown, this::newTodo);
        EventType.bind(toggleAll, change, event -> toggleAll());
        EventType.bind(clearCompleted, click, event -> clearCompleted());

        reset();
        repository.onExternalModification(this::reset);
    }

    private void reset() {
        Elements.removeChildrenFrom(list);
        for (Main.TodoItem item : repository.items()) {
            list.appendChild(new TodoItemElement(this, repository, item).asElement());
        }
        update();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }

    // ------------------------------------------------------ event / token handler

    private void newTodo(Event event) {
        KeyboardEvent keyboardEvent = (KeyboardEvent) event;
        if ("Enter".equals(keyboardEvent.key)) {
            String text = newTodo.value.trim();
            if (text.length() != 0) {
                Main.TodoItem item = repository.add(text);
                list.appendChild(new TodoItemElement(this, repository, item).asElement());
                newTodo.value = "";
                update();
            }
        }
    }

    private void toggleAll() {
        Main.toggleAll(list, toggleAll.checked);
        repository.completeAll(toggleAll.checked);
        update();
    }

    private void clearCompleted() {
        repository.removeAll(Main.getCompleted(list));
        update();
    }

    void filter(String token) {
        filter = Filter.parseToken(token);
        Main.filter(filter, filterAll, filterActive, filterCompleted);
        update();
    }

    // ------------------------------------------------------ state update

    void update() {
        Main.update(filter, list, main, footer, toggleAll, count, clearCompleted);
    }
}
