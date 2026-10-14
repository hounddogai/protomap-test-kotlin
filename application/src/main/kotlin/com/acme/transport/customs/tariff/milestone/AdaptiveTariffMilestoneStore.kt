package com.acme.transport.customs.tariff.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive tariff milestones by code. */
interface AdaptiveTariffMilestoneStore {
    fun find(code: String): AdaptiveTariffMilestone?
    fun save(item: AdaptiveTariffMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveTariffMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveTariffMilestoneStore(
    var items: MutableMap<String, AdaptiveTariffMilestone>,
    var order: MutableList<String>,
) : AdaptiveTariffMilestoneStore {
    override fun find(code: String): AdaptiveTariffMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveTariffMilestone) {
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

    override fun list(): MutableList<AdaptiveTariffMilestone> {
        val out = mutableListOf<AdaptiveTariffMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveTariffMilestoneStore(): AdaptiveTariffMilestoneStore {
    return MemoryAdaptiveTariffMilestoneStore(
        mutableMapOf<String, AdaptiveTariffMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveTariffMilestoneStore(
    var inner: AdaptiveTariffMilestoneStore,
    var cache: TtlCache<AdaptiveTariffMilestone>,
    var hits: Int,
    var misses: Int,
) : AdaptiveTariffMilestoneStore {
    override fun find(code: String): AdaptiveTariffMilestone? {
        val hit = (cache.get(code) as AdaptiveTariffMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveTariffMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveTariffMilestone> = inner.list()
}

fun newCachedAdaptiveTariffMilestoneStore(inner: AdaptiveTariffMilestoneStore, ttl: Int, clock: Clock): AdaptiveTariffMilestoneStore {
    return CachedAdaptiveTariffMilestoneStore(
        inner = inner,
        cache = newTtlCache<AdaptiveTariffMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive tariff milestones. Empty fields match all. */
data class AdaptiveTariffMilestoneQuery(
    var channel: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveTariffMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveTariffMilestone>): MutableList<AdaptiveTariffMilestone> {
        val matched = filter(items) { candidate: AdaptiveTariffMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveTariffMilestones(store: AdaptiveTariffMilestoneStore, query: AdaptiveTariffMilestoneQuery): MutableList<AdaptiveTariffMilestone> {
    return query.apply(store.list())
}
