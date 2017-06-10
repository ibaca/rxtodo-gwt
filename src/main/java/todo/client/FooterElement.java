package todo.client;

import static org.jboss.gwt.elemento.core.Elements.a;
import static org.jboss.gwt.elemento.core.Elements.p;
import static org.jboss.gwt.elemento.core.Elements.span;
import static todo.client.Main.i18n;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLStyleElement;
import elemental2.dom.ShadowRoot;
import jsinterop.annotations.JsType;
import org.jboss.gwt.elemento.core.Elements;

@JsType
public class FooterElement extends HTMLElement {

    public FooterElement() {
        ShadowRoot shadow = createShadowRoot();
        shadow.appendChild(Elements.htmlElement("style", HTMLStyleElement.class).textContent(""
                + ":host {\n"
                + "  margin: 65px auto 0;\n"
                + "  color: #bfbfbf;\n"
                + "  font-size: 10px;\n"
                + "  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);\n"
                + "  text-align: center;\n"
                + "  line-height: 1;\n"
                + "}\n"
                + ":host a {\n"
                + "  color: inherit;\n"
                + "  text-decoration: none;\n"
                + "  font-weight: 400;\n"
                + "}\n"
                + "\n"
                + ":host a:hover {\n"
                + "  text-decoration: underline;\n"
                + "}").asElement());
        shadow.appendChild(p().textContent(i18n.double_click_to_edit()).asElement());
        shadow.appendChild(p()
                .add(span().textContent(i18n.created_by() + " "))
                .add(a("https://github.com/ibaca").textContent("Ignacio Baca"))
                .asElement());
        shadow.appendChild(p()
                .add(span().textContent(i18n.part_of() + " "))
                .add(a("http://todomvc.com").textContent("TodoMVC"))
                .asElement());
    }
}
