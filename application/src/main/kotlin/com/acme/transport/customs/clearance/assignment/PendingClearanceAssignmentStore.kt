package com.acme.transport.customs.clearance.assignment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending clearance assignments by code. */
interface PendingClearanceAssignmentStore {
    fun find(code: String): PendingClearanceAssignment?
    fun save(item: PendingClearanceAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingClearanceAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingClearanceAssignmentStore(
    var items: MutableMap<String, PendingClearanceAssignment>,
    var order: MutableList<String>,
) : PendingClearanceAssignmentStore {
    override fun find(code: String): PendingClearanceAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingClearanceAssignment) {
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

    override fun list(): MutableList<PendingClearanceAssignment> {
        val out = mutableListOf<PendingClearanceAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingClearanceAssignmentStore(): PendingClearanceAssignmentStore {
    return MemoryPendingClearanceAssignmentStore(
        mutableMapOf<String, PendingClearanceAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingClearanceAssignmentStore(
    var inner: PendingClearanceAssignmentStore,
    var cache: TtlCache<PendingClearanceAssignment>,
    var hits: Int,
    var misses: Int,
) : PendingClearanceAssignmentStore {
    override fun find(code: String): PendingClearanceAssignment? {
        val hit = (cache.get(code) as PendingClearanceAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingClearanceAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingClearanceAssignment> = inner.list()
}

fun newCachedPendingClearanceAssignmentStore(inner: PendingClearanceAssignmentStore, ttl: Int, clock: Clock): PendingClearanceAssignmentStore {
    return CachedPendingClearanceAssignmentStore(
        inner = inner,
        cache = newTtlCache<PendingClearanceAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending clearance assignments. Empty fields match all. */
data class PendingClearanceAssignmentQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingClearanceAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingClearanceAssignment>): MutableList<PendingClearanceAssignment> {
        val matched = filter(items) { candidate: PendingClearanceAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingClearanceAssignments(store: PendingClearanceAssignmentStore, query: PendingClearanceAssignmentQuery): MutableList<PendingClearanceAssignment> {
    return query.apply(store.list())
}
