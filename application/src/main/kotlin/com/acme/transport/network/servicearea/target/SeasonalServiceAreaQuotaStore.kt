package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal service area quotas by code. */
interface SeasonalServiceAreaQuotaStore {
    fun find(code: String): SeasonalServiceAreaQuota?
    fun save(item: SeasonalServiceAreaQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServiceAreaQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServiceAreaQuotaStore(
    var items: MutableMap<String, SeasonalServiceAreaQuota>,
    var order: MutableList<String>,
) : SeasonalServiceAreaQuotaStore {
    override fun find(code: String): SeasonalServiceAreaQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServiceAreaQuota) {
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

    override fun list(): MutableList<SeasonalServiceAreaQuota> {
        val out = mutableListOf<SeasonalServiceAreaQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServiceAreaQuotaStore(): SeasonalServiceAreaQuotaStore {
    return MemorySeasonalServiceAreaQuotaStore(
        mutableMapOf<String, SeasonalServiceAreaQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServiceAreaQuotaStore(
    var inner: SeasonalServiceAreaQuotaStore,
    var cache: TtlCache<SeasonalServiceAreaQuota>,
    var hits: Int,
    var misses: Int,
) : SeasonalServiceAreaQuotaStore {
    override fun find(code: String): SeasonalServiceAreaQuota? {
        val hit = (cache.get(code) as SeasonalServiceAreaQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServiceAreaQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServiceAreaQuota> = inner.list()
}

fun newCachedSeasonalServiceAreaQuotaStore(inner: SeasonalServiceAreaQuotaStore, ttl: Int, clock: Clock): SeasonalServiceAreaQuotaStore {
    return CachedSeasonalServiceAreaQuotaStore(
        inner = inner,
        cache = newTtlCache<SeasonalServiceAreaQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service area quotas. Empty fields match all. */
data class SeasonalServiceAreaQuotaQuery(
    var segment: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServiceAreaQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalServiceAreaQuota>): MutableList<SeasonalServiceAreaQuota> {
        val matched = filter(items) { candidate: SeasonalServiceAreaQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServiceAreaQuotas(store: SeasonalServiceAreaQuotaStore, query: SeasonalServiceAreaQuotaQuery): MutableList<SeasonalServiceAreaQuota> {
    return query.apply(store.list())
}
