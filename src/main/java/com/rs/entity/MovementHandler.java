package com.rs.entity;
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

import com.rs.entity.player.Player;
import com.rs.util.Tickable;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Handles the movement of an {@link Entity}.
 *
 * @author blakeman8192
 */
public final class MovementHandler implements Tickable {

    private final Entity entity;
    private final Deque<Point> waypoints = new LinkedList<>();
    private boolean runPath = false;

    /**
     * Creates a new MovementHandler.
     */
    public MovementHandler(Entity entity) {
        this.entity = entity;
    }

    public void tick() {
        Point walkPoint;
        Point runPoint = null;

        // Handle the movement.
        walkPoint = waypoints.poll();

        // Handling run toggling
        if (entity instanceof Player) {
            Player player = (Player)entity;

            if (player.getAttributes().getSettings().isRunToggled() || isRunPath()) {
                if (player.getAttributes().hasRunEnergy()) {
                    runPoint = waypoints.poll();
                } else { // Player is out of energy
                    player.sendClientSetting(173, 0);
                    player.getAttributes().getSettings().setRunToggled(false);
                    setRunPath(false);
                    runPoint = null;
                }
            }
        } else if (isRunPath()) {
            runPoint = waypoints.poll();
        }

        // Walking
        if (walkPoint != null && walkPoint.getDirection() != -1) {
            entity.getPosition().move(Position.DIRECTION_DELTA_X[walkPoint.getDirection()], Position.DIRECTION_DELTA_Y[walkPoint.getDirection()]);
            entity.setPrimaryDirection(walkPoint.getDirection());
        }

        // Running
        if (runPoint != null && runPoint.getDirection() != -1) {
            entity.getPosition().move(Position.DIRECTION_DELTA_X[runPoint.getDirection()], Position.DIRECTION_DELTA_Y[runPoint.getDirection()]);
            entity.setSecondaryDirection(runPoint.getDirection());

            // Reducing energy
            if (entity instanceof Player) {
                Player player = (Player)entity;
                player.getAttributes().decreaseRunEnergy(player.getRunEnergyDecrement());
                player.sendRunEnergy();
            }
        } else {
            // Restoring run energy
            if (entity instanceof Player) {
                Player player = (Player)entity;

                if (player.getAttributes().getRunEnergy() != 100) {
                    player.getAttributes().increaseRunEnergy(player.getRunEnergyIncrement());
                    player.sendRunEnergy();
                }
            }
        }

        // Check for region changes.
        int deltaX = entity.getPosition().getX() - entity.getCurrentRegion().getRegionX() * 8;
        int deltaY = entity.getPosition().getY() - entity.getCurrentRegion().getRegionY() * 8;

        if (deltaX < 16 || deltaX >= 88 || deltaY < 16 || deltaY > 88) {
            if (entity instanceof Player) {
                ((Player)entity).sendMapRegion();
            }
        }
    }

    /**
     * Resets the walking queue.
     */
    public void reset() {
        setRunPath(false);
        waypoints.clear();

        // Set the base point as this position.
        Position p = entity.getPosition();
        waypoints.add(new Point(p.getX(), p.getY(), -1));
    }

    /**
     * Finishes the current path.
     */
    public void finish() {
        waypoints.removeFirst();
    }

    /**
     * Adds a position to the path.
     *
     * @param position the position
     */
    public void addToPath(Position position) {
        if (waypoints.size() == 0) {
            reset();
        }
        Point last = waypoints.peekLast();
        int deltaX = position.getX() - last.getX();
        int deltaY = position.getY() - last.getY();

        int max = Math.max(Math.abs(deltaX), Math.abs(deltaY));

        for (int i = 0; i < max; i++) {
            if (deltaX < 0) {
                deltaX++;
            } else if (deltaX > 0) {
                deltaX--;
            }

            if (deltaY < 0) {
                deltaY++;
            } else if (deltaY > 0) {
                deltaY--;
            }
            addStep(position.getX() - deltaX, position.getY() - deltaY);
        }
    }

    /**
     * Adds a step.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    private void addStep(int x, int y) {
        if (waypoints.size() >= 100) {
            return;
        }
        Point last = waypoints.peekLast();
        int deltaX = x - last.getX();
        int deltaY = y - last.getY();
        int direction = Position.direction(deltaX, deltaY);

        if (direction > -1) {
            waypoints.add(new Point(x, y, direction));
        }
    }

    /**
     * Gets whether or not we're running for the current path.
     */
    public boolean isRunPath() {
        return runPath;
    }

    /**
     * Toggles running for the current path only.
     */
    public void setRunPath(boolean runPath) {
        this.runPath = runPath;
    }

    /**
     * An internal Position type class with support for direction.
     *
     * @author blakeman8192
     */
    private class Point extends Position {

        private int direction;

        /**
         * Creates a new Point.
         *
         * @param x         the X coordinate
         * @param y         the Y coordinate
         * @param direction the direction to this point
         */
        public Point(int x, int y, int direction) {
            super(x, y);
            setDirection(direction);
        }

        /**
         * Gets the direction.
         *
         * @return the direction
         */
        public int getDirection() {
            return direction;
        }

        /**
         * Sets the direction.
         *
         * @param direction the direction
         */
        public void setDirection(int direction) {
            this.direction = direction;
        }

    }

}
