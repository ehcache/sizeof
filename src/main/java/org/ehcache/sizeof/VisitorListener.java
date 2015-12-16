package org.ehcache.sizeof;

/**
 * @author Alex Snaps
 */
abstract class VisitorListener {

    public abstract void visited(final Object object, final long size);
}
