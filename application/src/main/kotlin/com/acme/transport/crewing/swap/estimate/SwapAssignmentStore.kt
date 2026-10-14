package com.acme.transport.crewing.swap.estimate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap assignments by code. */
interface SwapAssignmentStore {
    fun find(code: String): SwapAssignment?
    fun save(item: SwapAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapAssignmentStore(
    var items: MutableMap<String, SwapAssignment>,
    var order: MutableList<String>,
) : SwapAssignmentStore {
    override fun find(code: String): SwapAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapAssignment) {
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

    override fun list(): MutableList<SwapAssignment> {
        val out = mutableListOf<SwapAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapAssignmentStore(): SwapAssignmentStore {
    return MemorySwapAssignmentStore(mutableMapOf<String, SwapAssignment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapAssignmentStore(
    var inner: SwapAssignmentStore,
    var cache: TtlCache<SwapAssignment>,
    var hits: Int,
    var misses: Int,
) : SwapAssignmentStore {
    override fun find(code: String): SwapAssignment? {
        val hit = (cache.get(code) as SwapAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapAssignment> = inner.list()
}

fun newCachedSwapAssignmentStore(inner: SwapAssignmentStore, ttl: Int, clock: Clock): SwapAssignmentStore {
    return CachedSwapAssignmentStore(
        inner = inner,
        cache = newTtlCache<SwapAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap assignments. Empty fields match all. */
data class SwapAssignmentQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SwapAssignment>): MutableList<SwapAssignment> {
        val matched = filter(items) { candidate: SwapAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapAssignments(store: SwapAssignmentStore, query: SwapAssignmentQuery): MutableList<SwapAssignment> {
    return query.apply(store.list())
}
