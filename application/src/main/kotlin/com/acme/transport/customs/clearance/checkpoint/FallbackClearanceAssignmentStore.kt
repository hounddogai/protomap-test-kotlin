package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback clearance assignments by code. */
interface FallbackClearanceAssignmentStore {
    fun find(code: String): FallbackClearanceAssignment?
    fun save(item: FallbackClearanceAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackClearanceAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackClearanceAssignmentStore(
    var items: MutableMap<String, FallbackClearanceAssignment>,
    var order: MutableList<String>,
) : FallbackClearanceAssignmentStore {
    override fun find(code: String): FallbackClearanceAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackClearanceAssignment) {
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

    override fun list(): MutableList<FallbackClearanceAssignment> {
        val out = mutableListOf<FallbackClearanceAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackClearanceAssignmentStore(): FallbackClearanceAssignmentStore {
    return MemoryFallbackClearanceAssignmentStore(
        mutableMapOf<String, FallbackClearanceAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackClearanceAssignmentStore(
    var inner: FallbackClearanceAssignmentStore,
    var cache: TtlCache<FallbackClearanceAssignment>,
    var hits: Int,
    var misses: Int,
) : FallbackClearanceAssignmentStore {
    override fun find(code: String): FallbackClearanceAssignment? {
        val hit = (cache.get(code) as FallbackClearanceAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackClearanceAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackClearanceAssignment> = inner.list()
}

fun newCachedFallbackClearanceAssignmentStore(inner: FallbackClearanceAssignmentStore, ttl: Int, clock: Clock): FallbackClearanceAssignmentStore {
    return CachedFallbackClearanceAssignmentStore(
        inner = inner,
        cache = newTtlCache<FallbackClearanceAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback clearance assignments. Empty fields match all. */
data class FallbackClearanceAssignmentQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackClearanceAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackClearanceAssignment>): MutableList<FallbackClearanceAssignment> {
        val matched = filter(items) { candidate: FallbackClearanceAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackClearanceAssignments(store: FallbackClearanceAssignmentStore, query: FallbackClearanceAssignmentQuery): MutableList<FallbackClearanceAssignment> {
    return query.apply(store.list())
}
