package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive backhaul milestones by code. */
interface AdaptiveBackhaulMilestoneStore {
    fun find(code: String): AdaptiveBackhaulMilestone?
    fun save(item: AdaptiveBackhaulMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBackhaulMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBackhaulMilestoneStore(
    var items: MutableMap<String, AdaptiveBackhaulMilestone>,
    var order: MutableList<String>,
) : AdaptiveBackhaulMilestoneStore {
    override fun find(code: String): AdaptiveBackhaulMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBackhaulMilestone) {
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

    override fun list(): MutableList<AdaptiveBackhaulMilestone> {
        val out = mutableListOf<AdaptiveBackhaulMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBackhaulMilestoneStore(): AdaptiveBackhaulMilestoneStore {
    return MemoryAdaptiveBackhaulMilestoneStore(
        mutableMapOf<String, AdaptiveBackhaulMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBackhaulMilestoneStore(
    var inner: AdaptiveBackhaulMilestoneStore,
    var cache: TtlCache<AdaptiveBackhaulMilestone>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBackhaulMilestoneStore {
    override fun find(code: String): AdaptiveBackhaulMilestone? {
        val hit = (cache.get(code) as AdaptiveBackhaulMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBackhaulMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBackhaulMilestone> = inner.list()
}

fun newCachedAdaptiveBackhaulMilestoneStore(inner: AdaptiveBackhaulMilestoneStore, ttl: Int, clock: Clock): AdaptiveBackhaulMilestoneStore {
    return CachedAdaptiveBackhaulMilestoneStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBackhaulMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive backhaul milestones. Empty fields match all. */
data class AdaptiveBackhaulMilestoneQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBackhaulMilestone): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveBackhaulMilestone>): MutableList<AdaptiveBackhaulMilestone> {
        val matched = filter(items) { candidate: AdaptiveBackhaulMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBackhaulMilestones(store: AdaptiveBackhaulMilestoneStore, query: AdaptiveBackhaulMilestoneQuery): MutableList<AdaptiveBackhaulMilestone> {
    return query.apply(store.list())
}
