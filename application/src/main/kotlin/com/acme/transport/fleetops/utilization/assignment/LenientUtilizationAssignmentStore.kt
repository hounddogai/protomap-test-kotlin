package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient utilization assignments by code. */
interface LenientUtilizationAssignmentStore {
    fun find(code: String): LenientUtilizationAssignment?
    fun save(item: LenientUtilizationAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientUtilizationAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientUtilizationAssignmentStore(
    var items: MutableMap<String, LenientUtilizationAssignment>,
    var order: MutableList<String>,
) : LenientUtilizationAssignmentStore {
    override fun find(code: String): LenientUtilizationAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientUtilizationAssignment) {
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

    override fun list(): MutableList<LenientUtilizationAssignment> {
        val out = mutableListOf<LenientUtilizationAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientUtilizationAssignmentStore(): LenientUtilizationAssignmentStore {
    return MemoryLenientUtilizationAssignmentStore(
        mutableMapOf<String, LenientUtilizationAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientUtilizationAssignmentStore(
    var inner: LenientUtilizationAssignmentStore,
    var cache: TtlCache<LenientUtilizationAssignment>,
    var hits: Int,
    var misses: Int,
) : LenientUtilizationAssignmentStore {
    override fun find(code: String): LenientUtilizationAssignment? {
        val hit = (cache.get(code) as LenientUtilizationAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientUtilizationAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientUtilizationAssignment> = inner.list()
}

fun newCachedLenientUtilizationAssignmentStore(inner: LenientUtilizationAssignmentStore, ttl: Int, clock: Clock): LenientUtilizationAssignmentStore {
    return CachedLenientUtilizationAssignmentStore(
        inner = inner,
        cache = newTtlCache<LenientUtilizationAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient utilization assignments. Empty fields match all. */
data class LenientUtilizationAssignmentQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientUtilizationAssignment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientUtilizationAssignment>): MutableList<LenientUtilizationAssignment> {
        val matched = filter(items) { candidate: LenientUtilizationAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientUtilizationAssignments(store: LenientUtilizationAssignmentStore, query: LenientUtilizationAssignmentQuery): MutableList<LenientUtilizationAssignment> {
    return query.apply(store.list())
}
