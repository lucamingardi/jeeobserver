/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 - 2012 Luca Mingardi.
 *
 * This file is part of jeeObserver.
 *
 * JeeObserver is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * JeeObserver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package jeeobserver.server;

import java.util.logging.Level;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * The Class JeeObserverServerListener.
 *
 * <p>
 * This servlet context listener start a new jeeObserver server instance at
 * application server startup.<br/>
 * Usually deployed into <strong>jeeobserver-server.war</strong> could be
 * included into any web other application.
 * </p>
 *
 * @author Luca Mingardi
 * @version 4.0
 */
public class JeeObserverServerListener implements ServletContextListener {

    private static int serverCount = 0;

    /**
     *
     * @param event
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {

        JeeObserverServerListener.serverCount = JeeObserverServerListener.serverCount - 1;

        if (JeeObserverServerListener.serverCount <= 0) {
            try {
                // Destroy JeeObserver server instance
                if (JeeObserverServerContext.getInstance() != null) {
                    JeeObserverServerContext.getInstance().close();
                }
            } catch (final JeeObserverServerException e) {
                JeeObserverServerContext.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     *
     * @param event
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        if (JeeObserverServerListener.serverCount == 0) {
            try {
                JeeObserverServerContext.createInstance();
            } catch (final JeeObserverServerException e) {
                JeeObserverServerContext.logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        JeeObserverServerListener.serverCount = JeeObserverServerListener.serverCount + 1;
    }
}
