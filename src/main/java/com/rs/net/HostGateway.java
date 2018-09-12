package com.rs.net;
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

import java.util.concurrent.ConcurrentHashMap;

/**
 * A static gateway type class that is used to limit the maximum amount of connections per host.
 *
 * @author blakeman8192
 */
public final class HostGateway {

    /**
     * Used to keep track of hosts and their amount of connections.
     */
    private static final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

    /**
     * Checks the host into the gateway.
     *
     * @param host the host
     * @return true if the host can connect, false if it has reached the maximum
     * amount of connections
     */
    public static void enter(String host) {
        if (map.containsKey(host)) {
            map.put(host, map.get(host) + 1);
        } else {
            map.put(host, 1);
        }
    }

    /**
     * Unchecks the host from the gateway.
     *
     * @param host the host
     */
    public static void exit(String host) {
        Integer amount = map.get(host);

        if (amount == null)
            return;

        // Remove the host from the map if it's at 1 connection.
        if (amount == 1) {
            map.remove(host);
            return;
        }

        // Otherwise decrement the amount of connections stored.
        map.put(host, amount - 1);
    }

    /**
     * @return The number of connections from the given host.
     */
    public static int count(String host) {
        return map.containsKey(host) ? map.get(host) : 0;
    }
}
