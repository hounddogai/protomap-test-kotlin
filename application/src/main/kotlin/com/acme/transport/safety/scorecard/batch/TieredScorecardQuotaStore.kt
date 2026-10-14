package com.acme.transport.safety.scorecard.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered scorecard quotas by code. */
interface TieredScorecardQuotaStore {
    fun find(code: String): TieredScorecardQuota?
    fun save(item: TieredScorecardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredScorecardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredScorecardQuotaStore(
    var items: MutableMap<String, TieredScorecardQuota>,
    var order: MutableList<String>,
) : TieredScorecardQuotaStore {
    override fun find(code: String): TieredScorecardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredScorecardQuota) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<TieredScorecardQuota> {
        val out = mutableListOf<TieredScorecardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredScorecardQuotaStore(): TieredScorecardQuotaStore {
    return MemoryTieredScorecardQuotaStore(
        mutableMapOf<String, TieredScorecardQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredScorecardQuotaStore(
    var inner: TieredScorecardQuotaStore,
    var cache: TtlCache<TieredScorecardQuota>,
    var hits: Int,
    var misses: Int,
) : TieredScorecardQuotaStore {
    override fun find(code: String): TieredScorecardQuota? {
        val hit = (cache.get(code) as TieredScorecardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredScorecardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredScorecardQuota> = inner.list()
}

fun newCachedTieredScorecardQuotaStore(inner: TieredScorecardQuotaStore, ttl: Int, clock: Clock): TieredScorecardQuotaStore {
    return CachedTieredScorecardQuotaStore(
        inner = inner,
        cache = newTtlCache<TieredScorecardQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered scorecard quotas. Empty fields match all. */
data class TieredScorecardQuotaQuery(
    var source: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredScorecardQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredScorecardQuota>): MutableList<TieredScorecardQuota> {
        val matched = filter(items) { candidate: TieredScorecardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredScorecardQuotas(store: TieredScorecardQuotaStore, query: TieredScorecardQuotaQuery): MutableList<TieredScorecardQuota> {
    return query.apply(store.list())
}
