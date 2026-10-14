package com.acme.transport.customs.clearance.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default clearance allocations by code. */
interface DefaultClearanceAllocationStore {
    fun find(code: String): DefaultClearanceAllocation?
    fun save(item: DefaultClearanceAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultClearanceAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultClearanceAllocationStore(
    var items: MutableMap<String, DefaultClearanceAllocation>,
    var order: MutableList<String>,
) : DefaultClearanceAllocationStore {
    override fun find(code: String): DefaultClearanceAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultClearanceAllocation) {
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

    override fun list(): MutableList<DefaultClearanceAllocation> {
        val out = mutableListOf<DefaultClearanceAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultClearanceAllocationStore(): DefaultClearanceAllocationStore {
    return MemoryDefaultClearanceAllocationStore(
        mutableMapOf<String, DefaultClearanceAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultClearanceAllocationStore(
    var inner: DefaultClearanceAllocationStore,
    var cache: TtlCache<DefaultClearanceAllocation>,
    var hits: Int,
    var misses: Int,
) : DefaultClearanceAllocationStore {
    override fun find(code: String): DefaultClearanceAllocation? {
        val hit = (cache.get(code) as DefaultClearanceAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultClearanceAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultClearanceAllocation> = inner.list()
}

fun newCachedDefaultClearanceAllocationStore(inner: DefaultClearanceAllocationStore, ttl: Int, clock: Clock): DefaultClearanceAllocationStore {
    return CachedDefaultClearanceAllocationStore(
        inner = inner,
        cache = newTtlCache<DefaultClearanceAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default clearance allocations. Empty fields match all. */
data class DefaultClearanceAllocationQuery(
    var channel: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultClearanceAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultClearanceAllocation>): MutableList<DefaultClearanceAllocation> {
        val matched = filter(items) { candidate: DefaultClearanceAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultClearanceAllocations(store: DefaultClearanceAllocationStore, query: DefaultClearanceAllocationQuery): MutableList<DefaultClearanceAllocation> {
    return query.apply(store.list())
}
