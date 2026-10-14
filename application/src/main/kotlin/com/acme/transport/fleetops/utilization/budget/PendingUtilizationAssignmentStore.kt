package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending utilization assignments by code. */
interface PendingUtilizationAssignmentStore {
    fun find(code: String): PendingUtilizationAssignment?
    fun save(item: PendingUtilizationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingUtilizationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingUtilizationAssignmentStore(
    var items: MutableMap<String, PendingUtilizationAssignment>,
    var order: MutableList<String>,
) : PendingUtilizationAssignmentStore {
    override fun find(code: String): PendingUtilizationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingUtilizationAssignment) {
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

    override fun list(): MutableList<PendingUtilizationAssignment> {
        val out = mutableListOf<PendingUtilizationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingUtilizationAssignmentStore(): PendingUtilizationAssignmentStore {
    return MemoryPendingUtilizationAssignmentStore(
        mutableMapOf<String, PendingUtilizationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingUtilizationAssignmentStore(
    var inner: PendingUtilizationAssignmentStore,
    var cache: TtlCache<PendingUtilizationAssignment>,
    var hits: Int,
    var misses: Int,
) : PendingUtilizationAssignmentStore {
    override fun find(code: String): PendingUtilizationAssignment? {
        val hit = (cache.get(code) as PendingUtilizationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingUtilizationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingUtilizationAssignment> = inner.list()
}

fun newCachedPendingUtilizationAssignmentStore(inner: PendingUtilizationAssignmentStore, ttl: Int, clock: Clock): PendingUtilizationAssignmentStore {
    return CachedPendingUtilizationAssignmentStore(
        inner = inner,
        cache = newTtlCache<PendingUtilizationAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending utilization assignments. Empty fields match all. */
data class PendingUtilizationAssignmentQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingUtilizationAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingUtilizationAssignment>): MutableList<PendingUtilizationAssignment> {
        val matched = filter(items) { candidate: PendingUtilizationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingUtilizationAssignments(store: PendingUtilizationAssignmentStore, query: PendingUtilizationAssignmentQuery): MutableList<PendingUtilizationAssignment> {
    return query.apply(store.list())
}
