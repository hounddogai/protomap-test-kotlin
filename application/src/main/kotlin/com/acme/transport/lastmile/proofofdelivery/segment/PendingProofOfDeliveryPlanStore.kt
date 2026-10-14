package com.acme.transport.lastmile.proofofdelivery.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending proof of delivery plans by code. */
interface PendingProofOfDeliveryPlanStore {
    fun find(code: String): PendingProofOfDeliveryPlan?
    fun save(item: PendingProofOfDeliveryPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingProofOfDeliveryPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingProofOfDeliveryPlanStore(
    var items: MutableMap<String, PendingProofOfDeliveryPlan>,
    var order: MutableList<String>,
) : PendingProofOfDeliveryPlanStore {
    override fun find(code: String): PendingProofOfDeliveryPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingProofOfDeliveryPlan) {
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

    override fun list(): MutableList<PendingProofOfDeliveryPlan> {
        val out = mutableListOf<PendingProofOfDeliveryPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingProofOfDeliveryPlanStore(): PendingProofOfDeliveryPlanStore {
    return MemoryPendingProofOfDeliveryPlanStore(
        mutableMapOf<String, PendingProofOfDeliveryPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingProofOfDeliveryPlanStore(
    var inner: PendingProofOfDeliveryPlanStore,
    var cache: TtlCache<PendingProofOfDeliveryPlan>,
    var hits: Int,
    var misses: Int,
) : PendingProofOfDeliveryPlanStore {
    override fun find(code: String): PendingProofOfDeliveryPlan? {
        val hit = (cache.get(code) as PendingProofOfDeliveryPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingProofOfDeliveryPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingProofOfDeliveryPlan> = inner.list()
}

fun newCachedPendingProofOfDeliveryPlanStore(inner: PendingProofOfDeliveryPlanStore, ttl: Int, clock: Clock): PendingProofOfDeliveryPlanStore {
    return CachedPendingProofOfDeliveryPlanStore(
        inner = inner,
        cache = newTtlCache<PendingProofOfDeliveryPlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending proof of delivery plans. Empty fields match all. */
data class PendingProofOfDeliveryPlanQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingProofOfDeliveryPlan): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingProofOfDeliveryPlan>): MutableList<PendingProofOfDeliveryPlan> {
        val matched = filter(items) { candidate: PendingProofOfDeliveryPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingProofOfDeliveryPlans(store: PendingProofOfDeliveryPlanStore, query: PendingProofOfDeliveryPlanQuery): MutableList<PendingProofOfDeliveryPlan> {
    return query.apply(store.list())
}
