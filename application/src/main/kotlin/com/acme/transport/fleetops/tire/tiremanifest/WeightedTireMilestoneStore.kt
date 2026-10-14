package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted tire milestones by code. */
interface WeightedTireMilestoneStore {
    fun find(code: String): WeightedTireMilestone?
    fun save(item: WeightedTireMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTireMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTireMilestoneStore(
    var items: MutableMap<String, WeightedTireMilestone>,
    var order: MutableList<String>,
) : WeightedTireMilestoneStore {
    override fun find(code: String): WeightedTireMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTireMilestone) {
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

    override fun list(): MutableList<WeightedTireMilestone> {
        val out = mutableListOf<WeightedTireMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTireMilestoneStore(): WeightedTireMilestoneStore {
    return MemoryWeightedTireMilestoneStore(
        mutableMapOf<String, WeightedTireMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTireMilestoneStore(
    var inner: WeightedTireMilestoneStore,
    var cache: TtlCache<WeightedTireMilestone>,
    var hits: Int,
    var misses: Int,
) : WeightedTireMilestoneStore {
    override fun find(code: String): WeightedTireMilestone? {
        val hit = (cache.get(code) as WeightedTireMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTireMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTireMilestone> = inner.list()
}

fun newCachedWeightedTireMilestoneStore(inner: WeightedTireMilestoneStore, ttl: Int, clock: Clock): WeightedTireMilestoneStore {
    return CachedWeightedTireMilestoneStore(
        inner = inner,
        cache = newTtlCache<WeightedTireMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted tire milestones. Empty fields match all. */
data class WeightedTireMilestoneQuery(
    var channel: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTireMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTireMilestone>): MutableList<WeightedTireMilestone> {
        val matched = filter(items) { candidate: WeightedTireMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTireMilestones(store: WeightedTireMilestoneStore, query: WeightedTireMilestoneQuery): MutableList<WeightedTireMilestone> {
    return query.apply(store.list())
}
