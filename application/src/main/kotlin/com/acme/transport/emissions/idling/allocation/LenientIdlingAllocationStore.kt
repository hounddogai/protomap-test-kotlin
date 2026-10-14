package com.acme.transport.emissions.idling.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient idling allocations by code. */
interface LenientIdlingAllocationStore {
    fun find(code: String): LenientIdlingAllocation?
    fun save(item: LenientIdlingAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientIdlingAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientIdlingAllocationStore(
    var items: MutableMap<String, LenientIdlingAllocation>,
    var order: MutableList<String>,
) : LenientIdlingAllocationStore {
    override fun find(code: String): LenientIdlingAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientIdlingAllocation) {
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

    override fun list(): MutableList<LenientIdlingAllocation> {
        val out = mutableListOf<LenientIdlingAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientIdlingAllocationStore(): LenientIdlingAllocationStore {
    return MemoryLenientIdlingAllocationStore(
        mutableMapOf<String, LenientIdlingAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientIdlingAllocationStore(
    var inner: LenientIdlingAllocationStore,
    var cache: TtlCache<LenientIdlingAllocation>,
    var hits: Int,
    var misses: Int,
) : LenientIdlingAllocationStore {
    override fun find(code: String): LenientIdlingAllocation? {
        val hit = (cache.get(code) as LenientIdlingAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientIdlingAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientIdlingAllocation> = inner.list()
}

fun newCachedLenientIdlingAllocationStore(inner: LenientIdlingAllocationStore, ttl: Int, clock: Clock): LenientIdlingAllocationStore {
    return CachedLenientIdlingAllocationStore(
        inner = inner,
        cache = newTtlCache<LenientIdlingAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient idling allocations. Empty fields match all. */
data class LenientIdlingAllocationQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientIdlingAllocation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientIdlingAllocation>): MutableList<LenientIdlingAllocation> {
        val matched = filter(items) { candidate: LenientIdlingAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientIdlingAllocations(store: LenientIdlingAllocationStore, query: LenientIdlingAllocationQuery): MutableList<LenientIdlingAllocation> {
    return query.apply(store.list())
}
