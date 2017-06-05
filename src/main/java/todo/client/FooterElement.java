package todo.client;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.footer;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static todo.client.Main.i18n;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;

public class FooterElement implements IsElement {
    private final HTMLElement root;

    public FooterElement() {
        root = footer().css("info")
                .add(p().textContent(i18n.double_click_to_edit()))
                .add(p().add(span().textContent(i18n.created_by() + " "))
                        .add(a("https://github.com/ibaca").textContent("Ignacio Baca")))
                .add(p().add(span().textContent(i18n.part_of() + " "))
                        .add(a("http://todomvc.com").textContent("TodoMVC")))
                .asElement();
    }

    @Override public HTMLElement asElement() { return root; }
}
