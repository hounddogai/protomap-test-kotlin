package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual service plan assignments by code. */
interface ManualServicePlanAssignmentStore {
    fun find(code: String): ManualServicePlanAssignment?
    fun save(item: ManualServicePlanAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServicePlanAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServicePlanAssignmentStore(
    var items: MutableMap<String, ManualServicePlanAssignment>,
    var order: MutableList<String>,
) : ManualServicePlanAssignmentStore {
    override fun find(code: String): ManualServicePlanAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServicePlanAssignment) {
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

    override fun list(): MutableList<ManualServicePlanAssignment> {
        val out = mutableListOf<ManualServicePlanAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServicePlanAssignmentStore(): ManualServicePlanAssignmentStore {
    return MemoryManualServicePlanAssignmentStore(
        mutableMapOf<String, ManualServicePlanAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServicePlanAssignmentStore(
    var inner: ManualServicePlanAssignmentStore,
    var cache: TtlCache<ManualServicePlanAssignment>,
    var hits: Int,
    var misses: Int,
) : ManualServicePlanAssignmentStore {
    override fun find(code: String): ManualServicePlanAssignment? {
        val hit = (cache.get(code) as ManualServicePlanAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServicePlanAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServicePlanAssignment> = inner.list()
}

fun newCachedManualServicePlanAssignmentStore(inner: ManualServicePlanAssignmentStore, ttl: Int, clock: Clock): ManualServicePlanAssignmentStore {
    return CachedManualServicePlanAssignmentStore(
        inner = inner,
        cache = newTtlCache<ManualServicePlanAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service plan assignments. Empty fields match all. */
data class ManualServicePlanAssignmentQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServicePlanAssignment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServicePlanAssignment>): MutableList<ManualServicePlanAssignment> {
        val matched = filter(items) { candidate: ManualServicePlanAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServicePlanAssignments(store: ManualServicePlanAssignmentStore, query: ManualServicePlanAssignmentQuery): MutableList<ManualServicePlanAssignment> {
    return query.apply(store.list())
}
