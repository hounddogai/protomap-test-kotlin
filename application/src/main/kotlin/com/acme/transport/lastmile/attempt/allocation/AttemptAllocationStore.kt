package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt allocations by code. */
interface AttemptAllocationStore {
    fun find(code: String): AttemptAllocation?
    fun save(item: AttemptAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptAllocationStore(
    var items: MutableMap<String, AttemptAllocation>,
    var order: MutableList<String>,
) : AttemptAllocationStore {
    override fun find(code: String): AttemptAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptAllocation) {
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

    override fun list(): MutableList<AttemptAllocation> {
        val out = mutableListOf<AttemptAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptAllocationStore(): AttemptAllocationStore {
    return MemoryAttemptAllocationStore(mutableMapOf<String, AttemptAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptAllocationStore(
    var inner: AttemptAllocationStore,
    var cache: TtlCache<AttemptAllocation>,
    var hits: Int,
    var misses: Int,
) : AttemptAllocationStore {
    override fun find(code: String): AttemptAllocation? {
        val hit = (cache.get(code) as AttemptAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptAllocation> = inner.list()
}

fun newCachedAttemptAllocationStore(inner: AttemptAllocationStore, ttl: Int, clock: Clock): AttemptAllocationStore {
    return CachedAttemptAllocationStore(
        inner = inner,
        cache = newTtlCache<AttemptAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt allocations. Empty fields match all. */
data class AttemptAllocationQuery(
    var lane: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptAllocation): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<AttemptAllocation>): MutableList<AttemptAllocation> {
        val matched = filter(items) { candidate: AttemptAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptAllocations(store: AttemptAllocationStore, query: AttemptAllocationQuery): MutableList<AttemptAllocation> {
    return query.apply(store.list())
}
