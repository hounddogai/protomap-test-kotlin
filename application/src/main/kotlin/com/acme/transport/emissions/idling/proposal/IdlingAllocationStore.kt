package com.acme.transport.emissions.idling.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling allocations by code. */
interface IdlingAllocationStore {
    fun find(code: String): IdlingAllocation?
    fun save(item: IdlingAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingAllocationStore(
    var items: MutableMap<String, IdlingAllocation>,
    var order: MutableList<String>,
) : IdlingAllocationStore {
    override fun find(code: String): IdlingAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingAllocation) {
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

    override fun list(): MutableList<IdlingAllocation> {
        val out = mutableListOf<IdlingAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingAllocationStore(): IdlingAllocationStore {
    return MemoryIdlingAllocationStore(mutableMapOf<String, IdlingAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingAllocationStore(
    var inner: IdlingAllocationStore,
    var cache: TtlCache<IdlingAllocation>,
    var hits: Int,
    var misses: Int,
) : IdlingAllocationStore {
    override fun find(code: String): IdlingAllocation? {
        val hit = (cache.get(code) as IdlingAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingAllocation> = inner.list()
}

fun newCachedIdlingAllocationStore(inner: IdlingAllocationStore, ttl: Int, clock: Clock): IdlingAllocationStore {
    return CachedIdlingAllocationStore(
        inner = inner,
        cache = newTtlCache<IdlingAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling allocations. Empty fields match all. */
data class IdlingAllocationQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingAllocation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<IdlingAllocation>): MutableList<IdlingAllocation> {
        val matched = filter(items) { candidate: IdlingAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingAllocations(store: IdlingAllocationStore, query: IdlingAllocationQuery): MutableList<IdlingAllocation> {
    return query.apply(store.list())
}
