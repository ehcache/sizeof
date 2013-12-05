package net.sf.ehcache.sizeofengine;

import net.sf.ehcache.pool.SizeOfEngineFactory;

/**
 * @author Alex Snaps
 */
public class EhcacheSizeOfEngineFactory implements SizeOfEngineFactory {

  private final EhcacheFilterSource ehcacheFilterSource;

  public EhcacheSizeOfEngineFactory() {
    this(new EhcacheFilterSource(true));
  }

  public EhcacheSizeOfEngineFactory(final EhcacheFilterSource ehcacheFilterSource) {
    this.ehcacheFilterSource = ehcacheFilterSource;
  }

  @Override
  public EhcacheSizeOfEngine createSizeOfEngine(final int maxDepth, final boolean abort, final boolean silent) {
    return new EhcacheSizeOfEngine(new Configuration(maxDepth, abort, silent, ehcacheFilterSource.getFilters()));
  }

  public Filter getFilter() {
    return ehcacheFilterSource;
  }
}
