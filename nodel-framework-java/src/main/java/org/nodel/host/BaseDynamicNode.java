package org.nodel.host;

/* 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;
import org.nodel.Handler;
import org.nodel.SimpleName;
import org.nodel.core.ActionRequestHandler;
import org.nodel.core.NodelServerAction;
import org.nodel.core.NodelServerEvent;
import org.nodel.reflection.Schema;

/**
 * A base for a dynamic node, i.e. one which has dynamic actions and events.
 */
public abstract class BaseDynamicNode extends BaseNode {
    
    public BaseDynamicNode(File root) throws IOException {
        super(root);
    }
    
    /**
     * Injects log into this Node on behalf of another entity (override to prevent)
     */
    public void injectLog(DateTime now, LogEntry.Source source, LogEntry.Type type, SimpleName alias, Object arg) {
        addLog(now, source, type, alias, arg);
    }
    
    /**
     * Injects an action on behalf of another entity (override to disallow)
     */
    public void injectLocalAction(NodelServerAction action) {
        addLocalAction(action);
    }
    
    public NodelServerAction injectLocalAction(String name, final Handler.H1<Object> handler, String desc, String group, String caution, double order, String argTitle, Class<?> argClass) {
        final SimpleName action = new SimpleName(name);
        
        Binding metadata = new Binding(action.getOriginalName(), desc, group, caution, order, Schema.getSchemaObject(argTitle, argClass));
        NodelServerAction nodelAction = new NodelServerAction(this.getName(), new SimpleName(action.getReducedName()), metadata);
        nodelAction.registerAction(new ActionRequestHandler() {
            
            @Override
            public void handleActionRequest(Object arg) {
                addLog(DateTime.now(), LogEntry.Source.local, LogEntry.Type.action, action, arg);
                
                handler.handle(arg);
            }
            
        });

        return addLocalAction(nodelAction);
    }
    
    /**
     * Extracts an action on behalf of another entity (override to disallow)
     */
    public void extractLocalAction(NodelServerAction action) {
        removeLocalAction(action);
    }
    
    /**
     * Injects an event on behalf of another entity (override to disallow)
     */
    public void injectLocalEvent(NodelServerEvent event) {
        addLocalEvent(event);
    }
    
    public NodelServerEvent injectLocalEvent(String name, String desc, String group, String caution, double order, String argTitle, Class<?> argClass) {
        final NodelServerEvent event = addLocalEvent(name, desc, group, caution, order, argTitle, argClass);
        event.attachMonitor(new Handler.H2<DateTime, Object>() {

            @Override
            public void handle(DateTime timestamp, Object arg) {
                addLog(timestamp, LogEntry.Source.local, LogEntry.Type.event, event.getEvent(), arg);
            }

        });
        return event;
    }
    
    /**
     * Removes an event on behalf of another entity (override to disallow)
     */
    public void extractLocalEvent(NodelServerEvent event) {
        removeLocalEvent(event);
    }    
   
}
