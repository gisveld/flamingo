/*
 * Copyright (C) 2012-2015 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.web.stripes;

import java.util.HashMap;
import java.util.Map;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.tag.DefaultPopulationStrategy;
import net.sourceforge.stripes.tag.InputTagSupport;
import net.sourceforge.stripes.tag.PopulationStrategy;
import net.sourceforge.stripes.util.Log;
/**
 * When you want to select the populationstrategy per actionbean, this the way to go:
 * Drop the SelectivePopulationStrategy into Stripes' extensions package and annotate your action bean.
    Assuming you want to use BeanFirstPopulationStrategy on your ExampleActionBean, it would look as follows.

    @CustomPopulationStrategy(BeanFirstPopulationStrategy.class)
    public class ExampleActionBean ...
    {
    ...
    }

    The other (unannotated) action beans will use the DefaultPopulationStrategy.
 * In web.xml you have to put r29 t/m r32 to make stripes load this package.
 * <filter>
        <display-name>Stripes Filter</display-name>
        <filter-name>StripesFilter</filter-name>
        <filter-class>net.sourceforge.stripes.controller.StripesFilter</filter-class>
        <init-param>
            <param-name>Extension.Packages</param-name>
            <param-value>nl.b3p.commons.stripes</param-value>
        </init-param>
    </filter>
 * @author Meine Toonen meinetoonen@b3partners.nl
 * 
 * Possible populationstrategies:
 * The DefaultPopulationStrategy searches in the following order for the first non-null value(s) when populating a given input tag:

        The HttpServletRequest parameter map for values matching the name of the input tag
        The ActionBean for a property or nested property matching the name of the input tag
        The value specified by the tag itself (varies by tag; usually as a value attribute, or as the body of the tag)

    The reasoning behind this is as follows:

        What the user entered should take precedence when re-displaying input to that same user
        Values in the ActionBean usually represent domain values, and are common sources or pre-population
        Values on the page are usually defaults specified for when no other applicable value is present

    Stripes also includes a second population strategy called BeanFirstPopulationStrategy. The semantics of this population strategy are quite different - it's search strategy is:

        If the field in question has errors, revert to the DefaultPopulationStrategy
        Otherwise if an ActionBean is present and has a matching property, use it's value even if it is null
        Otherwise look for a non-null value specified on the page
        And lastly, look for a non-null value in the HttpServletRequest

 * From: http://www.stripesframework.org/display/stripes/Overriding+PopulationStrategy+per+ActionBean
 */
public class SelectivePopulationStrategy implements PopulationStrategy {

    private static final Log LOG = Log.getInstance(SelectivePopulationStrategy.class);
    private Configuration config;
    private PopulationStrategy defaultDelegate;
    private Map<Class<? extends PopulationStrategy>, PopulationStrategy> delegates =
            new HashMap<Class<? extends PopulationStrategy>, PopulationStrategy>();
    private Map<Class<? extends ActionBean>, PopulationStrategy> actionBeanStrategies =
            new HashMap<Class<? extends ActionBean>, PopulationStrategy>();

    protected PopulationStrategy getDelegate(InputTagSupport tag)
            throws StripesJspException {
        ActionBean actionBean = tag.getActionBean();
        if (actionBean == null) {
            return defaultDelegate;
        }

        // check cache
        Class<? extends ActionBean> beanType = actionBean.getClass();
        PopulationStrategy delegate = actionBeanStrategies.get(beanType);
        if (delegate != null) {
            return delegate;
        }

        CustomPopulationStrategy annotation =
                beanType.getAnnotation(CustomPopulationStrategy.class);
        if (annotation == null) {
            delegate = defaultDelegate;
        } else {
            Class<? extends PopulationStrategy> type = annotation.value();
            delegate = delegates.get(type);
            if (delegate == null) {
                try {
                    delegate = type.newInstance();
                    delegate.init(config);
                    delegates.put(type, delegate);
                } catch (Exception e) {
                    delegate = defaultDelegate;
                    LOG.info("Could not instantiate population strategy"
                            + " of name [" + type + "]", e);
                }
            }
        }

        // cache and return
        actionBeanStrategies.put(beanType, delegate);
        return delegate;
    }

    public Object getValue(InputTagSupport tag) throws StripesJspException {
        PopulationStrategy strategy = getDelegate(tag);
        Object value = (strategy).getValue(tag);
        return value;
    }

    public void init(Configuration configuration) throws Exception {
        this.config = configuration;
        defaultDelegate = new DefaultPopulationStrategy();
        defaultDelegate.init(config);
    }
}