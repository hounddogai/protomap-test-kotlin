package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery allocations by code. */
interface ProofOfDeliveryAllocationStore {
    fun find(code: String): ProofOfDeliveryAllocation?
    fun save(item: ProofOfDeliveryAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryAllocationStore(
    var items: MutableMap<String, ProofOfDeliveryAllocation>,
    var order: MutableList<String>,
) : ProofOfDeliveryAllocationStore {
    override fun find(code: String): ProofOfDeliveryAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryAllocation) {
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

    override fun list(): MutableList<ProofOfDeliveryAllocation> {
        val out = mutableListOf<ProofOfDeliveryAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryAllocationStore(): ProofOfDeliveryAllocationStore {
    return MemoryProofOfDeliveryAllocationStore(
        mutableMapOf<String, ProofOfDeliveryAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryAllocationStore(
    var inner: ProofOfDeliveryAllocationStore,
    var cache: TtlCache<ProofOfDeliveryAllocation>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryAllocationStore {
    override fun find(code: String): ProofOfDeliveryAllocation? {
        val hit = (cache.get(code) as ProofOfDeliveryAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryAllocation> = inner.list()
}

fun newCachedProofOfDeliveryAllocationStore(inner: ProofOfDeliveryAllocationStore, ttl: Int, clock: Clock): ProofOfDeliveryAllocationStore {
    return CachedProofOfDeliveryAllocationStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryAllocation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery allocations. Empty fields match all. */
data class ProofOfDeliveryAllocationQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryAllocation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ProofOfDeliveryAllocation>): MutableList<ProofOfDeliveryAllocation> {
        val matched = filter(items) { candidate: ProofOfDeliveryAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryAllocations(store: ProofOfDeliveryAllocationStore, query: ProofOfDeliveryAllocationQuery): MutableList<ProofOfDeliveryAllocation> {
    return query.apply(store.list())
}
