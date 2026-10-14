package com.acme.transport.fleetops.tire.quota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal tire quotas by code. */
interface SeasonalTireQuotaStore {
    fun find(code: String): SeasonalTireQuota?
    fun save(item: SeasonalTireQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTireQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTireQuotaStore(
    var items: MutableMap<String, SeasonalTireQuota>,
    var order: MutableList<String>,
) : SeasonalTireQuotaStore {
    override fun find(code: String): SeasonalTireQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTireQuota) {
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

    override fun list(): MutableList<SeasonalTireQuota> {
        val out = mutableListOf<SeasonalTireQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTireQuotaStore(): SeasonalTireQuotaStore {
    return MemorySeasonalTireQuotaStore(mutableMapOf<String, SeasonalTireQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTireQuotaStore(
    var inner: SeasonalTireQuotaStore,
    var cache: TtlCache<SeasonalTireQuota>,
    var hits: Int,
    var misses: Int,
) : SeasonalTireQuotaStore {
    override fun find(code: String): SeasonalTireQuota? {
        val hit = (cache.get(code) as SeasonalTireQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTireQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTireQuota> = inner.list()
}

fun newCachedSeasonalTireQuotaStore(inner: SeasonalTireQuotaStore, ttl: Int, clock: Clock): SeasonalTireQuotaStore {
    return CachedSeasonalTireQuotaStore(
        inner = inner,
        cache = newTtlCache<SeasonalTireQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tire quotas. Empty fields match all. */
data class SeasonalTireQuotaQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTireQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTireQuota>): MutableList<SeasonalTireQuota> {
        val matched = filter(items) { candidate: SeasonalTireQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTireQuotas(store: SeasonalTireQuotaStore, query: SeasonalTireQuotaQuery): MutableList<SeasonalTireQuota> {
    return query.apply(store.list())
}
