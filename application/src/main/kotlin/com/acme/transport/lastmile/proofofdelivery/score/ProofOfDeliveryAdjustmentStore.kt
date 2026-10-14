package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery adjustments by code. */
interface ProofOfDeliveryAdjustmentStore {
    fun find(code: String): ProofOfDeliveryAdjustment?
    fun save(item: ProofOfDeliveryAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryAdjustmentStore(
    var items: MutableMap<String, ProofOfDeliveryAdjustment>,
    var order: MutableList<String>,
) : ProofOfDeliveryAdjustmentStore {
    override fun find(code: String): ProofOfDeliveryAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryAdjustment) {
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

    override fun list(): MutableList<ProofOfDeliveryAdjustment> {
        val out = mutableListOf<ProofOfDeliveryAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryAdjustmentStore(): ProofOfDeliveryAdjustmentStore {
    return MemoryProofOfDeliveryAdjustmentStore(
        mutableMapOf<String, ProofOfDeliveryAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryAdjustmentStore(
    var inner: ProofOfDeliveryAdjustmentStore,
    var cache: TtlCache<ProofOfDeliveryAdjustment>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryAdjustmentStore {
    override fun find(code: String): ProofOfDeliveryAdjustment? {
        val hit = (cache.get(code) as ProofOfDeliveryAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryAdjustment> = inner.list()
}

fun newCachedProofOfDeliveryAdjustmentStore(inner: ProofOfDeliveryAdjustmentStore, ttl: Int, clock: Clock): ProofOfDeliveryAdjustmentStore {
    return CachedProofOfDeliveryAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery adjustments. Empty fields match all. */
data class ProofOfDeliveryAdjustmentQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryAdjustment>): MutableList<ProofOfDeliveryAdjustment> {
        val matched = filter(items) { candidate: ProofOfDeliveryAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryAdjustments(store: ProofOfDeliveryAdjustmentStore, query: ProofOfDeliveryAdjustmentQuery): MutableList<ProofOfDeliveryAdjustment> {
    return query.apply(store.list())
}
