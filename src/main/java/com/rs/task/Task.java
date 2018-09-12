package com.rs.task;
/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.rs.util.Tickable;
import com.rs.entity.player.Player;

/**
 * A simple task base class.
 *
 * @author Pure_
 */
public abstract class Task implements Tickable {

    private boolean active;
    private int currentDelay;
    private final int delay;
    private final boolean runOnce;
    private final Player player;
    private final Object[] args;
    private int ticks;

    /**
     * Creates a new task instance which will run once with no arguments.
     */
    public Task(int delay, Player player) {
        this(delay, true, player);
    }

    /**
     * Creates a new task instance.
     *
     * @param delay   the delay (in server ticks) until the task is to be executed
     * @param runOnce if the task should only be executed once
     * @param player  the player this task is bound to
     * @param args    the args to provide to the instance
     */
    public Task(int delay, boolean runOnce, Player player, Object... args) {
        this.delay = delay;
        this.runOnce = runOnce;
        this.player = player;
        this.args = args;
        this.active = true;
        currentDelay = delay;
    }

    /**
     * Contains the task's executable code, this is executed once the delay is depleted.
     */
    protected abstract void process();

    /**
     * Performs a logic tick, this checks if the task can be executed yet.
     */
    public void tick() throws Exception {
        if (active && currentDelay-- <= 0) {
            process();
            ticks++;
            currentDelay = delay;

            if (runOnce) {
                active = false;
            }
        }
    }

    /**
     * Whether or not the task should be removed next tick.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the task as inactive.
     */
    public void setInactive() {
        this.active = false;
    }

    /**
     * Gets the array of arguments passed to the task upon creation.
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * The player this task is bound to.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The amount of times this task has been executed.
     */
    public int getTicks() {
        return ticks;
    }
}
