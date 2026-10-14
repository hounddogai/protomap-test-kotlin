package com.acme.transport.crewing.dutyperiod.periodproposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional duty period tiers by code. */
interface RegionalDutyPeriodTierStore {
    fun find(code: String): RegionalDutyPeriodTier?
    fun save(item: RegionalDutyPeriodTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDutyPeriodTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDutyPeriodTierStore(
    var items: MutableMap<String, RegionalDutyPeriodTier>,
    var order: MutableList<String>,
) : RegionalDutyPeriodTierStore {
    override fun find(code: String): RegionalDutyPeriodTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDutyPeriodTier) {
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

    override fun list(): MutableList<RegionalDutyPeriodTier> {
        val out = mutableListOf<RegionalDutyPeriodTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDutyPeriodTierStore(): RegionalDutyPeriodTierStore {
    return MemoryRegionalDutyPeriodTierStore(
        mutableMapOf<String, RegionalDutyPeriodTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDutyPeriodTierStore(
    var inner: RegionalDutyPeriodTierStore,
    var cache: TtlCache<RegionalDutyPeriodTier>,
    var hits: Int,
    var misses: Int,
) : RegionalDutyPeriodTierStore {
    override fun find(code: String): RegionalDutyPeriodTier? {
        val hit = (cache.get(code) as RegionalDutyPeriodTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDutyPeriodTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDutyPeriodTier> = inner.list()
}

fun newCachedRegionalDutyPeriodTierStore(inner: RegionalDutyPeriodTierStore, ttl: Int, clock: Clock): RegionalDutyPeriodTierStore {
    return CachedRegionalDutyPeriodTierStore(
        inner = inner,
        cache = newTtlCache<RegionalDutyPeriodTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional duty period tiers. Empty fields match all. */
data class RegionalDutyPeriodTierQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDutyPeriodTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDutyPeriodTier>): MutableList<RegionalDutyPeriodTier> {
        val matched = filter(items) { candidate: RegionalDutyPeriodTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDutyPeriodTiers(store: RegionalDutyPeriodTierStore, query: RegionalDutyPeriodTierQuery): MutableList<RegionalDutyPeriodTier> {
    return query.apply(store.list())
}
