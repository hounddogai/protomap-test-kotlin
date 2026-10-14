package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection assignments by code. */
interface InspectionAssignmentStore {
    fun find(code: String): InspectionAssignment?
    fun save(item: InspectionAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionAssignmentStore(
    var items: MutableMap<String, InspectionAssignment>,
    var order: MutableList<String>,
) : InspectionAssignmentStore {
    override fun find(code: String): InspectionAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionAssignment) {
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

    override fun list(): MutableList<InspectionAssignment> {
        val out = mutableListOf<InspectionAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionAssignmentStore(): InspectionAssignmentStore {
    return MemoryInspectionAssignmentStore(
        mutableMapOf<String, InspectionAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionAssignmentStore(
    var inner: InspectionAssignmentStore,
    var cache: TtlCache<InspectionAssignment>,
    var hits: Int,
    var misses: Int,
) : InspectionAssignmentStore {
    override fun find(code: String): InspectionAssignment? {
        val hit = (cache.get(code) as InspectionAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionAssignment> = inner.list()
}

fun newCachedInspectionAssignmentStore(inner: InspectionAssignmentStore, ttl: Int, clock: Clock): InspectionAssignmentStore {
    return CachedInspectionAssignmentStore(
        inner = inner,
        cache = newTtlCache<InspectionAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection assignments. Empty fields match all. */
data class InspectionAssignmentQuery(
    var country: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionAssignment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionAssignment>): MutableList<InspectionAssignment> {
        val matched = filter(items) { candidate: InspectionAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionAssignments(store: InspectionAssignmentStore, query: InspectionAssignmentQuery): MutableList<InspectionAssignment> {
    return query.apply(store.list())
}
