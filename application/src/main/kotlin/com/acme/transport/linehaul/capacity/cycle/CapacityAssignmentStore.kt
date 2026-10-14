package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity assignments by code. */
interface CapacityAssignmentStore {
    fun find(code: String): CapacityAssignment?
    fun save(item: CapacityAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityAssignmentStore(
    var items: MutableMap<String, CapacityAssignment>,
    var order: MutableList<String>,
) : CapacityAssignmentStore {
    override fun find(code: String): CapacityAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityAssignment) {
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

    override fun list(): MutableList<CapacityAssignment> {
        val out = mutableListOf<CapacityAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityAssignmentStore(): CapacityAssignmentStore {
    return MemoryCapacityAssignmentStore(mutableMapOf<String, CapacityAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityAssignmentStore(
    var inner: CapacityAssignmentStore,
    var cache: TtlCache<CapacityAssignment>,
    var hits: Int,
    var misses: Int,
) : CapacityAssignmentStore {
    override fun find(code: String): CapacityAssignment? {
        val hit = (cache.get(code) as CapacityAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityAssignment> = inner.list()
}

fun newCachedCapacityAssignmentStore(inner: CapacityAssignmentStore, ttl: Int, clock: Clock): CapacityAssignmentStore {
    return CachedCapacityAssignmentStore(
        inner = inner,
        cache = newTtlCache<CapacityAssignment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity assignments. Empty fields match all. */
data class CapacityAssignmentQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityAssignment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityAssignment>): MutableList<CapacityAssignment> {
        val matched = filter(items) { candidate: CapacityAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityAssignments(store: CapacityAssignmentStore, query: CapacityAssignmentQuery): MutableList<CapacityAssignment> {
    return query.apply(store.list())
}
