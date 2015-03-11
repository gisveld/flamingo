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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import net.sourceforge.stripes.tag.DefaultPopulationStrategy;
import net.sourceforge.stripes.tag.PopulationStrategy;

/**
 * From: http://www.stripesframework.org/display/stripes/Overriding+PopulationStrategy+per+ActionBean
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomPopulationStrategy 
{
  Class<? extends PopulationStrategy> value() 
    default DefaultPopulationStrategy.class;
}