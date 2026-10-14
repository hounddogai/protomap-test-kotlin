package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection milestones by code. */
interface AdaptiveInspectionMilestoneStore {
    fun find(code: String): AdaptiveInspectionMilestone?
    fun save(item: AdaptiveInspectionMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionMilestoneStore(
    var items: MutableMap<String, AdaptiveInspectionMilestone>,
    var order: MutableList<String>,
) : AdaptiveInspectionMilestoneStore {
    override fun find(code: String): AdaptiveInspectionMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionMilestone) {
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

    override fun list(): MutableList<AdaptiveInspectionMilestone> {
        val out = mutableListOf<AdaptiveInspectionMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionMilestoneStore(): AdaptiveInspectionMilestoneStore {
    return MemoryAdaptiveInspectionMilestoneStore(
        mutableMapOf<String, AdaptiveInspectionMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionMilestoneStore(
    var inner: AdaptiveInspectionMilestoneStore,
    var cache: TtlCache<AdaptiveInspectionMilestone>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionMilestoneStore {
    override fun find(code: String): AdaptiveInspectionMilestone? {
        val hit = (cache.get(code) as AdaptiveInspectionMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionMilestone> = inner.list()
}

fun newCachedAdaptiveInspectionMilestoneStore(inner: AdaptiveInspectionMilestoneStore, ttl: Int, clock: Clock): AdaptiveInspectionMilestoneStore {
    return CachedAdaptiveInspectionMilestoneStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection milestones. Empty fields match all. */
data class AdaptiveInspectionMilestoneQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionMilestone): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionMilestone>): MutableList<AdaptiveInspectionMilestone> {
        val matched = filter(items) { candidate: AdaptiveInspectionMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionMilestones(store: AdaptiveInspectionMilestoneStore, query: AdaptiveInspectionMilestoneQuery): MutableList<AdaptiveInspectionMilestone> {
    return query.apply(store.list())
}
