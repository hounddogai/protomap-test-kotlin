package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute allocations by code. */
interface DisputeAllocationStore {
    fun find(code: String): DisputeAllocation?
    fun save(item: DisputeAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeAllocationStore(
    var items: MutableMap<String, DisputeAllocation>,
    var order: MutableList<String>,
) : DisputeAllocationStore {
    override fun find(code: String): DisputeAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeAllocation) {
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

    override fun list(): MutableList<DisputeAllocation> {
        val out = mutableListOf<DisputeAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeAllocationStore(): DisputeAllocationStore {
    return MemoryDisputeAllocationStore(mutableMapOf<String, DisputeAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeAllocationStore(
    var inner: DisputeAllocationStore,
    var cache: TtlCache<DisputeAllocation>,
    var hits: Int,
    var misses: Int,
) : DisputeAllocationStore {
    override fun find(code: String): DisputeAllocation? {
        val hit = (cache.get(code) as DisputeAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeAllocation> = inner.list()
}

fun newCachedDisputeAllocationStore(inner: DisputeAllocationStore, ttl: Int, clock: Clock): DisputeAllocationStore {
    return CachedDisputeAllocationStore(
        inner = inner,
        cache = newTtlCache<DisputeAllocation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute allocations. Empty fields match all. */
data class DisputeAllocationQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeAllocation>): MutableList<DisputeAllocation> {
        val matched = filter(items) { candidate: DisputeAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeAllocations(store: DisputeAllocationStore, query: DisputeAllocationQuery): MutableList<DisputeAllocation> {
    return query.apply(store.list())
}
