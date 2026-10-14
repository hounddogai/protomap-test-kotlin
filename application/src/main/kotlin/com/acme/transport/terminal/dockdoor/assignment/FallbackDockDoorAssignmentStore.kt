package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback dock door assignments by code. */
interface FallbackDockDoorAssignmentStore {
    fun find(code: String): FallbackDockDoorAssignment?
    fun save(item: FallbackDockDoorAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDockDoorAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDockDoorAssignmentStore(
    var items: MutableMap<String, FallbackDockDoorAssignment>,
    var order: MutableList<String>,
) : FallbackDockDoorAssignmentStore {
    override fun find(code: String): FallbackDockDoorAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDockDoorAssignment) {
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

    override fun list(): MutableList<FallbackDockDoorAssignment> {
        val out = mutableListOf<FallbackDockDoorAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDockDoorAssignmentStore(): FallbackDockDoorAssignmentStore {
    return MemoryFallbackDockDoorAssignmentStore(
        mutableMapOf<String, FallbackDockDoorAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDockDoorAssignmentStore(
    var inner: FallbackDockDoorAssignmentStore,
    var cache: TtlCache<FallbackDockDoorAssignment>,
    var hits: Int,
    var misses: Int,
) : FallbackDockDoorAssignmentStore {
    override fun find(code: String): FallbackDockDoorAssignment? {
        val hit = (cache.get(code) as FallbackDockDoorAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDockDoorAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDockDoorAssignment> = inner.list()
}

fun newCachedFallbackDockDoorAssignmentStore(inner: FallbackDockDoorAssignmentStore, ttl: Int, clock: Clock): FallbackDockDoorAssignmentStore {
    return CachedFallbackDockDoorAssignmentStore(
        inner = inner,
        cache = newTtlCache<FallbackDockDoorAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dock door assignments. Empty fields match all. */
data class FallbackDockDoorAssignmentQuery(
    var unit: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDockDoorAssignment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDockDoorAssignment>): MutableList<FallbackDockDoorAssignment> {
        val matched = filter(items) { candidate: FallbackDockDoorAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDockDoorAssignments(store: FallbackDockDoorAssignmentStore, query: FallbackDockDoorAssignmentQuery): MutableList<FallbackDockDoorAssignment> {
    return query.apply(store.list())
}
