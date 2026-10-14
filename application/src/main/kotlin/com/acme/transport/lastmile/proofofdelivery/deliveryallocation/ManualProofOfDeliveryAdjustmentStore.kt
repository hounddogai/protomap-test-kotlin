package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual proof of delivery adjustments by code. */
interface ManualProofOfDeliveryAdjustmentStore {
    fun find(code: String): ManualProofOfDeliveryAdjustment?
    fun save(item: ManualProofOfDeliveryAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualProofOfDeliveryAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualProofOfDeliveryAdjustmentStore(
    var items: MutableMap<String, ManualProofOfDeliveryAdjustment>,
    var order: MutableList<String>,
) : ManualProofOfDeliveryAdjustmentStore {
    override fun find(code: String): ManualProofOfDeliveryAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualProofOfDeliveryAdjustment) {
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

    override fun list(): MutableList<ManualProofOfDeliveryAdjustment> {
        val out = mutableListOf<ManualProofOfDeliveryAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualProofOfDeliveryAdjustmentStore(): ManualProofOfDeliveryAdjustmentStore {
    return MemoryManualProofOfDeliveryAdjustmentStore(
        mutableMapOf<String, ManualProofOfDeliveryAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualProofOfDeliveryAdjustmentStore(
    var inner: ManualProofOfDeliveryAdjustmentStore,
    var cache: TtlCache<ManualProofOfDeliveryAdjustment>,
    var hits: Int,
    var misses: Int,
) : ManualProofOfDeliveryAdjustmentStore {
    override fun find(code: String): ManualProofOfDeliveryAdjustment? {
        val hit = (cache.get(code) as ManualProofOfDeliveryAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualProofOfDeliveryAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualProofOfDeliveryAdjustment> = inner.list()
}

fun newCachedManualProofOfDeliveryAdjustmentStore(inner: ManualProofOfDeliveryAdjustmentStore, ttl: Int, clock: Clock): ManualProofOfDeliveryAdjustmentStore {
    return CachedManualProofOfDeliveryAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ManualProofOfDeliveryAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual proof of delivery adjustments. Empty fields match all. */
data class ManualProofOfDeliveryAdjustmentQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualProofOfDeliveryAdjustment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<ManualProofOfDeliveryAdjustment>): MutableList<ManualProofOfDeliveryAdjustment> {
        val matched = filter(items) { candidate: ManualProofOfDeliveryAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualProofOfDeliveryAdjustments(store: ManualProofOfDeliveryAdjustmentStore, query: ManualProofOfDeliveryAdjustmentQuery): MutableList<ManualProofOfDeliveryAdjustment> {
    return query.apply(store.list())
}
