package com.acme.transport.customs.inspection.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection milestones by code. */
interface InspectionMilestoneStore {
    fun find(code: String): InspectionMilestone?
    fun save(item: InspectionMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionMilestoneStore(
    var items: MutableMap<String, InspectionMilestone>,
    var order: MutableList<String>,
) : InspectionMilestoneStore {
    override fun find(code: String): InspectionMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionMilestone) {
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

    override fun list(): MutableList<InspectionMilestone> {
        val out = mutableListOf<InspectionMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionMilestoneStore(): InspectionMilestoneStore {
    return MemoryInspectionMilestoneStore(mutableMapOf<String, InspectionMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionMilestoneStore(
    var inner: InspectionMilestoneStore,
    var cache: TtlCache<InspectionMilestone>,
    var hits: Int,
    var misses: Int,
) : InspectionMilestoneStore {
    override fun find(code: String): InspectionMilestone? {
        val hit = (cache.get(code) as InspectionMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionMilestone> = inner.list()
}

fun newCachedInspectionMilestoneStore(inner: InspectionMilestoneStore, ttl: Int, clock: Clock): InspectionMilestoneStore {
    return CachedInspectionMilestoneStore(
        inner = inner,
        cache = newTtlCache<InspectionMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection milestones. Empty fields match all. */
data class InspectionMilestoneQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionMilestone>): MutableList<InspectionMilestone> {
        val matched = filter(items) { candidate: InspectionMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionMilestones(store: InspectionMilestoneStore, query: InspectionMilestoneQuery): MutableList<InspectionMilestone> {
    return query.apply(store.list())
}
