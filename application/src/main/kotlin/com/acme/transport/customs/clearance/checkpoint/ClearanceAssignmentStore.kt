package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance assignments by code. */
interface ClearanceAssignmentStore {
    fun find(code: String): ClearanceAssignment?
    fun save(item: ClearanceAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceAssignmentStore(
    var items: MutableMap<String, ClearanceAssignment>,
    var order: MutableList<String>,
) : ClearanceAssignmentStore {
    override fun find(code: String): ClearanceAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceAssignment) {
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

    override fun list(): MutableList<ClearanceAssignment> {
        val out = mutableListOf<ClearanceAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceAssignmentStore(): ClearanceAssignmentStore {
    return MemoryClearanceAssignmentStore(mutableMapOf<String, ClearanceAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceAssignmentStore(
    var inner: ClearanceAssignmentStore,
    var cache: TtlCache<ClearanceAssignment>,
    var hits: Int,
    var misses: Int,
) : ClearanceAssignmentStore {
    override fun find(code: String): ClearanceAssignment? {
        val hit = (cache.get(code) as ClearanceAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceAssignment> = inner.list()
}

fun newCachedClearanceAssignmentStore(inner: ClearanceAssignmentStore, ttl: Int, clock: Clock): ClearanceAssignmentStore {
    return CachedClearanceAssignmentStore(
        inner = inner,
        cache = newTtlCache<ClearanceAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance assignments. Empty fields match all. */
data class ClearanceAssignmentQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceAssignment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceAssignment>): MutableList<ClearanceAssignment> {
        val matched = filter(items) { candidate: ClearanceAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceAssignments(store: ClearanceAssignmentStore, query: ClearanceAssignmentQuery): MutableList<ClearanceAssignment> {
    return query.apply(store.list())
}
