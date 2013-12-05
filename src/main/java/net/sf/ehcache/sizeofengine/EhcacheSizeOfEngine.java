package net.sf.ehcache.sizeofengine;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.pool.Size;
import net.sf.ehcache.pool.SizeOfEngine;
import net.sf.ehcache.pool.sizeof.AgentSizeOf;
import net.sf.ehcache.pool.sizeof.MaxDepthExceededException;
import net.sf.ehcache.pool.sizeof.ReflectionSizeOf;
import net.sf.ehcache.pool.sizeof.SizeOf;
import net.sf.ehcache.pool.sizeof.UnsafeSizeOf;
import net.sf.ehcache.pool.sizeof.filter.CombinationSizeOfFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alex Snaps
 */
public class EhcacheSizeOfEngine implements SizeOfEngine {

  private static final Logger LOG = LoggerFactory.getLogger(EhcacheSizeOfEngine.class.getName());
  private static final String VERBOSE_DEBUG_LOGGING = "net.sf.ehcache.sizeof.verboseDebugLogging";

  private static final boolean USE_VERBOSE_DEBUG_LOGGING = Boolean.getBoolean(VERBOSE_DEBUG_LOGGING);

  private final Configuration cfg;
  private final SizeOf sizeOf;

  public EhcacheSizeOfEngine(Configuration cfg) {
    this.cfg = cfg;
    SizeOf bestSizeOf;
    try {
      bestSizeOf = new AgentSizeOf(new CombinationSizeOfFilter(cfg.getFilters()));
      if (!cfg.isSilent()) {
        LOG.info("using Agent sizeof engine");
      }
    } catch (UnsupportedOperationException e) {
      try {
        bestSizeOf = new UnsafeSizeOf(new CombinationSizeOfFilter(cfg.getFilters()));
        if (!cfg.isSilent()) {
          LOG.info("using Unsafe sizeof engine");
        }
      } catch (UnsupportedOperationException f) {
        try {
          bestSizeOf = new ReflectionSizeOf(new CombinationSizeOfFilter(cfg.getFilters()));
          if (!cfg.isSilent()) {
            LOG.info("using Reflection sizeof engine");
          }
        } catch (UnsupportedOperationException g) {
          throw new CacheException("A suitable SizeOf engine could not be loaded: " + e + ", " + f + ", " + g);
        }
      }
    }

    this.sizeOf = bestSizeOf;
  }

  public Configuration getConfiguration() {
    return cfg;
  }

  /**
   * {@inheritDoc}
   */
  public SizeOfEngine copyWith(int maxDepth, boolean abortWhenMaxDepthExceeded) {
    return new EhcacheSizeOfEngine(this.cfg);
  }

  /**
   * {@inheritDoc}
   */
  public Size sizeOf(final Object key, final Object value, final Object container) {
    Size size;
    try {
      size = sizeOf.deepSizeOf(cfg.getMaxDepth(), cfg.isAbort(), key, value, container);
    } catch (MaxDepthExceededException e) {
      LOG.warn(e.getMessage());
      LOG.warn("key type: " + key.getClass().getName());
      LOG.warn("key: " + key);
      LOG.warn("value type: " + value.getClass().getName());
      LOG.warn("value: " + value);
      LOG.warn("container: " + container);
      size = new Size(e.getMeasuredSize(), false);
    }

    if (USE_VERBOSE_DEBUG_LOGGING && LOG.isDebugEnabled()) {
      LOG.debug("size of {}/{}/{} -> {}", new Object[] { key, value, container, size.getCalculated() });
    }
    return size;
  }
}
