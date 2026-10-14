package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps seasonal violation quotas by code. */
interface SeasonalViolationQuotaStore {
    fun find(code: String): SeasonalViolationQuota?
    fun save(item: SeasonalViolationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalViolationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalViolationQuotaStore(
    var items: MutableMap<String, SeasonalViolationQuota>,
    var order: MutableList<String>,
) : SeasonalViolationQuotaStore {
    override fun find(code: String): SeasonalViolationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalViolationQuota) {
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

    override fun list(): MutableList<SeasonalViolationQuota> {
        val out = mutableListOf<SeasonalViolationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalViolationQuotaStore(): SeasonalViolationQuotaStore {
    return MemorySeasonalViolationQuotaStore(
        mutableMapOf<String, SeasonalViolationQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalViolationQuotaStore(
    var inner: SeasonalViolationQuotaStore,
    var cache: TtlCache<SeasonalViolationQuota>,
    var hits: Int,
    var misses: Int,
) : SeasonalViolationQuotaStore {
    override fun find(code: String): SeasonalViolationQuota? {
        val hit = (cache.get(code) as SeasonalViolationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalViolationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalViolationQuota> = inner.list()
}

fun newCachedSeasonalViolationQuotaStore(inner: SeasonalViolationQuotaStore, ttl: Int, clock: Clock): SeasonalViolationQuotaStore {
    return CachedSeasonalViolationQuotaStore(
        inner = inner,
        cache = newTtlCache<SeasonalViolationQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal violation quotas. Empty fields match all. */
data class SeasonalViolationQuotaQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalViolationQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SeasonalViolationQuota>): MutableList<SeasonalViolationQuota> {
        val matched = filter(items) { candidate: SeasonalViolationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalViolationQuotas(store: SeasonalViolationQuotaStore, query: SeasonalViolationQuotaQuery): MutableList<SeasonalViolationQuota> {
    return query.apply(store.list())
}
