package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period tiers by code. */
interface DutyPeriodTierStore {
    fun find(code: String): DutyPeriodTier?
    fun save(item: DutyPeriodTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodTierStore(
    var items: MutableMap<String, DutyPeriodTier>,
    var order: MutableList<String>,
) : DutyPeriodTierStore {
    override fun find(code: String): DutyPeriodTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodTier) {
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

    override fun list(): MutableList<DutyPeriodTier> {
        val out = mutableListOf<DutyPeriodTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodTierStore(): DutyPeriodTierStore {
    return MemoryDutyPeriodTierStore(mutableMapOf<String, DutyPeriodTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodTierStore(
    var inner: DutyPeriodTierStore,
    var cache: TtlCache<DutyPeriodTier>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodTierStore {
    override fun find(code: String): DutyPeriodTier? {
        val hit = (cache.get(code) as DutyPeriodTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodTier> = inner.list()
}

fun newCachedDutyPeriodTierStore(inner: DutyPeriodTierStore, ttl: Int, clock: Clock): DutyPeriodTierStore {
    return CachedDutyPeriodTierStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period tiers. Empty fields match all. */
data class DutyPeriodTierQuery(
    var channel: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodTier): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodTier>): MutableList<DutyPeriodTier> {
        val matched = filter(items) { candidate: DutyPeriodTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodTiers(store: DutyPeriodTierStore, query: DutyPeriodTierQuery): MutableList<DutyPeriodTier> {
    return query.apply(store.list())
}
