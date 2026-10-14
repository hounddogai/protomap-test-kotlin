package com.acme.transport.fleetops.defect.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect assignments by code. */
interface DefectAssignmentStore {
    fun find(code: String): DefectAssignment?
    fun save(item: DefectAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectAssignmentStore(
    var items: MutableMap<String, DefectAssignment>,
    var order: MutableList<String>,
) : DefectAssignmentStore {
    override fun find(code: String): DefectAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectAssignment) {
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

    override fun list(): MutableList<DefectAssignment> {
        val out = mutableListOf<DefectAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectAssignmentStore(): DefectAssignmentStore {
    return MemoryDefectAssignmentStore(mutableMapOf<String, DefectAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectAssignmentStore(
    var inner: DefectAssignmentStore,
    var cache: TtlCache<DefectAssignment>,
    var hits: Int,
    var misses: Int,
) : DefectAssignmentStore {
    override fun find(code: String): DefectAssignment? {
        val hit = (cache.get(code) as DefectAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectAssignment> = inner.list()
}

fun newCachedDefectAssignmentStore(inner: DefectAssignmentStore, ttl: Int, clock: Clock): DefectAssignmentStore {
    return CachedDefectAssignmentStore(
        inner = inner,
        cache = newTtlCache<DefectAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect assignments. Empty fields match all. */
data class DefectAssignmentQuery(
    var region: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectAssignment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectAssignment>): MutableList<DefectAssignment> {
        val matched = filter(items) { candidate: DefectAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectAssignments(store: DefectAssignmentStore, query: DefectAssignmentQuery): MutableList<DefectAssignment> {
    return query.apply(store.list())
}
