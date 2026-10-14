package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery allocations by code. */
interface RedeliveryAllocationStore {
    fun find(code: String): RedeliveryAllocation?
    fun save(item: RedeliveryAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryAllocationStore(
    var items: MutableMap<String, RedeliveryAllocation>,
    var order: MutableList<String>,
) : RedeliveryAllocationStore {
    override fun find(code: String): RedeliveryAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryAllocation) {
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

    override fun list(): MutableList<RedeliveryAllocation> {
        val out = mutableListOf<RedeliveryAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryAllocationStore(): RedeliveryAllocationStore {
    return MemoryRedeliveryAllocationStore(
        mutableMapOf<String, RedeliveryAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryAllocationStore(
    var inner: RedeliveryAllocationStore,
    var cache: TtlCache<RedeliveryAllocation>,
    var hits: Int,
    var misses: Int,
) : RedeliveryAllocationStore {
    override fun find(code: String): RedeliveryAllocation? {
        val hit = (cache.get(code) as RedeliveryAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryAllocation> = inner.list()
}

fun newCachedRedeliveryAllocationStore(inner: RedeliveryAllocationStore, ttl: Int, clock: Clock): RedeliveryAllocationStore {
    return CachedRedeliveryAllocationStore(
        inner = inner,
        cache = newTtlCache<RedeliveryAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery allocations. Empty fields match all. */
data class RedeliveryAllocationQuery(
    var channel: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryAllocation>): MutableList<RedeliveryAllocation> {
        val matched = filter(items) { candidate: RedeliveryAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryAllocations(store: RedeliveryAllocationStore, query: RedeliveryAllocationQuery): MutableList<RedeliveryAllocation> {
    return query.apply(store.list())
}
