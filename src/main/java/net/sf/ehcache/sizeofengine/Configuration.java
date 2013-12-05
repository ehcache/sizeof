package net.sf.ehcache.sizeofengine;

import net.sf.ehcache.pool.sizeof.filter.SizeOfFilter;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Alex Snaps
 */
public final class Configuration {
  private final int maxDepth;
  private final boolean abort;
  private final boolean silent;
  private final SizeOfFilter[] filters;

  public Configuration(final int maxDepth, final boolean abort, final boolean silent, final SizeOfFilter... filters) {
    this.maxDepth = maxDepth;
    this.abort = abort;
    this.silent = silent;
    this.filters = filters;
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public boolean isAbort() {
    return abort;
  }

  public boolean isSilent() {
    return silent;
  }

  public SizeOfFilter[] getFilters() {
    return filters;
  }

  public static final class Builder {

    private int maxDepth;
    private boolean silent;
    private boolean abort;
    private ArrayList<SizeOfFilter> filters = new ArrayList<SizeOfFilter>();

    public Builder() {
    }

    public Builder(Configuration cfg) {
      maxDepth(cfg.maxDepth);
      silent(cfg.silent);
      abort(cfg.abort);
      Collections.addAll(filters, cfg.filters);
    }

    public Builder maxDepth(int maxDepth) {
      this.maxDepth = maxDepth;
      return this;
    }

    public Builder silent(boolean silent) {
      this.silent = silent;
      return this;
    }

    public Builder abort(boolean abort) {
      this.abort = abort;
      return this;
    }

    public Builder addFilter(SizeOfFilter filter) {
      if (!filters.contains(filter)) {
        filters.add(filter);
      }
      return this;
    }

    public Builder addFilters(SizeOfFilter... filters) {
      for (SizeOfFilter filter : filters) {
        addFilter(filter);
      }
      return this;
    }

    public Builder removeFilter(SizeOfFilter filter) {
      filters.remove(filter);
      return this;
    }

    public Builder removeFilters(SizeOfFilter... filters) {
      for (SizeOfFilter filter : filters) {
        this.filters.remove(filter);
      }
      return this;
    }

    public Builder clearlFilters() {
      this.filters.clear();
      return this;
    }

    public Configuration build() {
      return new Configuration(maxDepth, abort, silent, filters.toArray(new SizeOfFilter[filters.size()]));
    }
  }
}
