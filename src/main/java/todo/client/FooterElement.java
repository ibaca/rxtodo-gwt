package todo.client;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.footer;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static todo.client.Main.i18n;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import jsinterop.base.Js;

public class FooterElement extends Widget {
    public FooterElement() {
        setElement(Js.<Element>cast(footer().css("info")
                .add(p().textContent(i18n.double_click_to_edit()))
                .add(p().add(span().textContent(i18n.created_by() + " "))
                        .add(a("https://github.com/ibaca").textContent("Ignacio Baca")))
                .add(p().add(span().textContent(i18n.part_of() + " "))
                        .add(a("http://todomvc.com").textContent("TodoMVC")))
                .asElement()));
    }
}
