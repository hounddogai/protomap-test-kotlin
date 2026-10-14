package com.acme.transport.lastmile.proofofdelivery.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery batches by code. */
interface ProofOfDeliveryBatchStore {
    fun find(code: String): ProofOfDeliveryBatch?
    fun save(item: ProofOfDeliveryBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryBatchStore(
    var items: MutableMap<String, ProofOfDeliveryBatch>,
    var order: MutableList<String>,
) : ProofOfDeliveryBatchStore {
    override fun find(code: String): ProofOfDeliveryBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryBatch) {
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

    override fun list(): MutableList<ProofOfDeliveryBatch> {
        val out = mutableListOf<ProofOfDeliveryBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryBatchStore(): ProofOfDeliveryBatchStore {
    return MemoryProofOfDeliveryBatchStore(
        mutableMapOf<String, ProofOfDeliveryBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryBatchStore(
    var inner: ProofOfDeliveryBatchStore,
    var cache: TtlCache<ProofOfDeliveryBatch>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryBatchStore {
    override fun find(code: String): ProofOfDeliveryBatch? {
        val hit = (cache.get(code) as ProofOfDeliveryBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryBatch> = inner.list()
}

fun newCachedProofOfDeliveryBatchStore(inner: ProofOfDeliveryBatchStore, ttl: Int, clock: Clock): ProofOfDeliveryBatchStore {
    return CachedProofOfDeliveryBatchStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery batches. Empty fields match all. */
data class ProofOfDeliveryBatchQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryBatch): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryBatch>): MutableList<ProofOfDeliveryBatch> {
        val matched = filter(items) { candidate: ProofOfDeliveryBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryBatches(store: ProofOfDeliveryBatchStore, query: ProofOfDeliveryBatchQuery): MutableList<ProofOfDeliveryBatch> {
    return query.apply(store.list())
}
