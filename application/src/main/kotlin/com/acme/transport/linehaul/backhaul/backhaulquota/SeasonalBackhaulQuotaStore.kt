package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal backhaul quotas by code. */
interface SeasonalBackhaulQuotaStore {
    fun find(code: String): SeasonalBackhaulQuota?
    fun save(item: SeasonalBackhaulQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalBackhaulQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalBackhaulQuotaStore(
    var items: MutableMap<String, SeasonalBackhaulQuota>,
    var order: MutableList<String>,
) : SeasonalBackhaulQuotaStore {
    override fun find(code: String): SeasonalBackhaulQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalBackhaulQuota) {
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

    override fun list(): MutableList<SeasonalBackhaulQuota> {
        val out = mutableListOf<SeasonalBackhaulQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalBackhaulQuotaStore(): SeasonalBackhaulQuotaStore {
    return MemorySeasonalBackhaulQuotaStore(
        mutableMapOf<String, SeasonalBackhaulQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalBackhaulQuotaStore(
    var inner: SeasonalBackhaulQuotaStore,
    var cache: TtlCache<SeasonalBackhaulQuota>,
    var hits: Int,
    var misses: Int,
) : SeasonalBackhaulQuotaStore {
    override fun find(code: String): SeasonalBackhaulQuota? {
        val hit = (cache.get(code) as SeasonalBackhaulQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalBackhaulQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalBackhaulQuota> = inner.list()
}

fun newCachedSeasonalBackhaulQuotaStore(inner: SeasonalBackhaulQuotaStore, ttl: Int, clock: Clock): SeasonalBackhaulQuotaStore {
    return CachedSeasonalBackhaulQuotaStore(
        inner = inner,
        cache = newTtlCache<SeasonalBackhaulQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal backhaul quotas. Empty fields match all. */
data class SeasonalBackhaulQuotaQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalBackhaulQuota): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalBackhaulQuota>): MutableList<SeasonalBackhaulQuota> {
        val matched = filter(items) { candidate: SeasonalBackhaulQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalBackhaulQuotas(store: SeasonalBackhaulQuotaStore, query: SeasonalBackhaulQuotaQuery): MutableList<SeasonalBackhaulQuota> {
    return query.apply(store.list())
}
