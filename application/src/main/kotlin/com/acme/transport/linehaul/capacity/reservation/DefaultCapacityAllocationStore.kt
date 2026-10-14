package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default capacity allocations by code. */
interface DefaultCapacityAllocationStore {
    fun find(code: String): DefaultCapacityAllocation?
    fun save(item: DefaultCapacityAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCapacityAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCapacityAllocationStore(
    var items: MutableMap<String, DefaultCapacityAllocation>,
    var order: MutableList<String>,
) : DefaultCapacityAllocationStore {
    override fun find(code: String): DefaultCapacityAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCapacityAllocation) {
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

    override fun list(): MutableList<DefaultCapacityAllocation> {
        val out = mutableListOf<DefaultCapacityAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCapacityAllocationStore(): DefaultCapacityAllocationStore {
    return MemoryDefaultCapacityAllocationStore(
        mutableMapOf<String, DefaultCapacityAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCapacityAllocationStore(
    var inner: DefaultCapacityAllocationStore,
    var cache: TtlCache<DefaultCapacityAllocation>,
    var hits: Int,
    var misses: Int,
) : DefaultCapacityAllocationStore {
    override fun find(code: String): DefaultCapacityAllocation? {
        val hit = (cache.get(code) as DefaultCapacityAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCapacityAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCapacityAllocation> = inner.list()
}

fun newCachedDefaultCapacityAllocationStore(inner: DefaultCapacityAllocationStore, ttl: Int, clock: Clock): DefaultCapacityAllocationStore {
    return CachedDefaultCapacityAllocationStore(
        inner = inner,
        cache = newTtlCache<DefaultCapacityAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default capacity allocations. Empty fields match all. */
data class DefaultCapacityAllocationQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCapacityAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DefaultCapacityAllocation>): MutableList<DefaultCapacityAllocation> {
        val matched = filter(items) { candidate: DefaultCapacityAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCapacityAllocations(store: DefaultCapacityAllocationStore, query: DefaultCapacityAllocationQuery): MutableList<DefaultCapacityAllocation> {
    return query.apply(store.list())
}
