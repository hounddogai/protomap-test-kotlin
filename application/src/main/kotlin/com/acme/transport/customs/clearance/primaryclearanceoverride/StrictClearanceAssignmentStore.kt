package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict clearance assignments by code. */
interface StrictClearanceAssignmentStore {
    fun find(code: String): StrictClearanceAssignment?
    fun save(item: StrictClearanceAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictClearanceAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictClearanceAssignmentStore(
    var items: MutableMap<String, StrictClearanceAssignment>,
    var order: MutableList<String>,
) : StrictClearanceAssignmentStore {
    override fun find(code: String): StrictClearanceAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictClearanceAssignment) {
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

    override fun list(): MutableList<StrictClearanceAssignment> {
        val out = mutableListOf<StrictClearanceAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictClearanceAssignmentStore(): StrictClearanceAssignmentStore {
    return MemoryStrictClearanceAssignmentStore(
        mutableMapOf<String, StrictClearanceAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictClearanceAssignmentStore(
    var inner: StrictClearanceAssignmentStore,
    var cache: TtlCache<StrictClearanceAssignment>,
    var hits: Int,
    var misses: Int,
) : StrictClearanceAssignmentStore {
    override fun find(code: String): StrictClearanceAssignment? {
        val hit = (cache.get(code) as StrictClearanceAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictClearanceAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictClearanceAssignment> = inner.list()
}

fun newCachedStrictClearanceAssignmentStore(inner: StrictClearanceAssignmentStore, ttl: Int, clock: Clock): StrictClearanceAssignmentStore {
    return CachedStrictClearanceAssignmentStore(
        inner = inner,
        cache = newTtlCache<StrictClearanceAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict clearance assignments. Empty fields match all. */
data class StrictClearanceAssignmentQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictClearanceAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<StrictClearanceAssignment>): MutableList<StrictClearanceAssignment> {
        val matched = filter(items) { candidate: StrictClearanceAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictClearanceAssignments(store: StrictClearanceAssignmentStore, query: StrictClearanceAssignmentQuery): MutableList<StrictClearanceAssignment> {
    return query.apply(store.list())
}
