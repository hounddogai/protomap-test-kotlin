package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal parcel quotas by code. */
interface SeasonalParcelQuotaStore {
    fun find(code: String): SeasonalParcelQuota?
    fun save(item: SeasonalParcelQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalParcelQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalParcelQuotaStore(
    var items: MutableMap<String, SeasonalParcelQuota>,
    var order: MutableList<String>,
) : SeasonalParcelQuotaStore {
    override fun find(code: String): SeasonalParcelQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalParcelQuota) {
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

    override fun list(): MutableList<SeasonalParcelQuota> {
        val out = mutableListOf<SeasonalParcelQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalParcelQuotaStore(): SeasonalParcelQuotaStore {
    return MemorySeasonalParcelQuotaStore(mutableMapOf<String, SeasonalParcelQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalParcelQuotaStore(
    var inner: SeasonalParcelQuotaStore,
    var cache: TtlCache<SeasonalParcelQuota>,
    var hits: Int,
    var misses: Int,
) : SeasonalParcelQuotaStore {
    override fun find(code: String): SeasonalParcelQuota? {
        val hit = (cache.get(code) as SeasonalParcelQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalParcelQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalParcelQuota> = inner.list()
}

fun newCachedSeasonalParcelQuotaStore(inner: SeasonalParcelQuotaStore, ttl: Int, clock: Clock): SeasonalParcelQuotaStore {
    return CachedSeasonalParcelQuotaStore(
        inner = inner,
        cache = newTtlCache<SeasonalParcelQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal parcel quotas. Empty fields match all. */
data class SeasonalParcelQuotaQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalParcelQuota): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<SeasonalParcelQuota>): MutableList<SeasonalParcelQuota> {
        val matched = filter(items) { candidate: SeasonalParcelQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalParcelQuotas(store: SeasonalParcelQuotaStore, query: SeasonalParcelQuotaQuery): MutableList<SeasonalParcelQuota> {
    return query.apply(store.list())
}
