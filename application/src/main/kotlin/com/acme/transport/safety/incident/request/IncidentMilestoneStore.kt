package com.acme.transport.safety.incident.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident milestones by code. */
interface IncidentMilestoneStore {
    fun find(code: String): IncidentMilestone?
    fun save(item: IncidentMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentMilestoneStore(
    var items: MutableMap<String, IncidentMilestone>,
    var order: MutableList<String>,
) : IncidentMilestoneStore {
    override fun find(code: String): IncidentMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentMilestone) {
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

    override fun list(): MutableList<IncidentMilestone> {
        val out = mutableListOf<IncidentMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentMilestoneStore(): IncidentMilestoneStore {
    return MemoryIncidentMilestoneStore(mutableMapOf<String, IncidentMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentMilestoneStore(
    var inner: IncidentMilestoneStore,
    var cache: TtlCache<IncidentMilestone>,
    var hits: Int,
    var misses: Int,
) : IncidentMilestoneStore {
    override fun find(code: String): IncidentMilestone? {
        val hit = (cache.get(code) as IncidentMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentMilestone> = inner.list()
}

fun newCachedIncidentMilestoneStore(inner: IncidentMilestoneStore, ttl: Int, clock: Clock): IncidentMilestoneStore {
    return CachedIncidentMilestoneStore(
        inner = inner,
        cache = newTtlCache<IncidentMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident milestones. Empty fields match all. */
data class IncidentMilestoneQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<IncidentMilestone>): MutableList<IncidentMilestone> {
        val matched = filter(items) { candidate: IncidentMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentMilestones(store: IncidentMilestoneStore, query: IncidentMilestoneQuery): MutableList<IncidentMilestone> {
    return query.apply(store.list())
}
