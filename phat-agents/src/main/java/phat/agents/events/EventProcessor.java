/*
 * Copyright (C) 2014 Pablo Campillo-Sanchez <pabcampi@ucm.es>
 *
 * This software has been developed as part of the 
 * SociAAL project directed by Jorge J. Gomez Sanz
 * (http://grasia.fdi.ucm.es/sociaal)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package phat.agents.events;

import java.lang.reflect.InvocationTargetException;

import phat.agents.Agent;
import phat.agents.automaton.Automaton;
import phat.agents.automaton.conditions.AutomatonCondition;

public class EventProcessor {

    String eventId;
    AutomatonCondition condition;
    Class<? extends Automaton> activity;

    public EventProcessor(String eventId, AutomatonCondition condition,
            Class<? extends Automaton> activity) {
        super();
        this.eventId = eventId;
        this.condition = condition;
        this.activity = activity;
    }

    public Automaton process(Agent agent) {
        if (condition == null || condition.evaluate(agent)) {
            try {
                System.out.println(eventId+": "+activity.getClass().getSimpleName());
                return activity.getConstructor(Agent.class, String.class).
                        newInstance(agent, activity.getClass().getSimpleName());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getEventId() {
        return eventId;
    }
}
