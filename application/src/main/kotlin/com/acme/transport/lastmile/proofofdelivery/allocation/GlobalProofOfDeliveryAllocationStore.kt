package com.acme.transport.lastmile.proofofdelivery.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global proof of delivery allocations by code. */
interface GlobalProofOfDeliveryAllocationStore {
    fun find(code: String): GlobalProofOfDeliveryAllocation?
    fun save(item: GlobalProofOfDeliveryAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalProofOfDeliveryAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalProofOfDeliveryAllocationStore(
    var items: MutableMap<String, GlobalProofOfDeliveryAllocation>,
    var order: MutableList<String>,
) : GlobalProofOfDeliveryAllocationStore {
    override fun find(code: String): GlobalProofOfDeliveryAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalProofOfDeliveryAllocation) {
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

    override fun list(): MutableList<GlobalProofOfDeliveryAllocation> {
        val out = mutableListOf<GlobalProofOfDeliveryAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalProofOfDeliveryAllocationStore(): GlobalProofOfDeliveryAllocationStore {
    return MemoryGlobalProofOfDeliveryAllocationStore(
        mutableMapOf<String, GlobalProofOfDeliveryAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalProofOfDeliveryAllocationStore(
    var inner: GlobalProofOfDeliveryAllocationStore,
    var cache: TtlCache<GlobalProofOfDeliveryAllocation>,
    var hits: Int,
    var misses: Int,
) : GlobalProofOfDeliveryAllocationStore {
    override fun find(code: String): GlobalProofOfDeliveryAllocation? {
        val hit = (cache.get(code) as GlobalProofOfDeliveryAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalProofOfDeliveryAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalProofOfDeliveryAllocation> = inner.list()
}

fun newCachedGlobalProofOfDeliveryAllocationStore(inner: GlobalProofOfDeliveryAllocationStore, ttl: Int, clock: Clock): GlobalProofOfDeliveryAllocationStore {
    return CachedGlobalProofOfDeliveryAllocationStore(
        inner = inner,
        cache = newTtlCache<GlobalProofOfDeliveryAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global proof of delivery allocations. Empty fields match all. */
data class GlobalProofOfDeliveryAllocationQuery(
    var channel: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalProofOfDeliveryAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalProofOfDeliveryAllocation>): MutableList<GlobalProofOfDeliveryAllocation> {
        val matched = filter(items) { candidate: GlobalProofOfDeliveryAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalProofOfDeliveryAllocations(store: GlobalProofOfDeliveryAllocationStore, query: GlobalProofOfDeliveryAllocationQuery): MutableList<GlobalProofOfDeliveryAllocation> {
    return query.apply(store.list())
}
