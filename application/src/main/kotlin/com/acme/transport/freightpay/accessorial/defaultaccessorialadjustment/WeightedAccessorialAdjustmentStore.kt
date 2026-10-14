package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted accessorial adjustments by code. */
interface WeightedAccessorialAdjustmentStore {
    fun find(code: String): WeightedAccessorialAdjustment?
    fun save(item: WeightedAccessorialAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAccessorialAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAccessorialAdjustmentStore(
    var items: MutableMap<String, WeightedAccessorialAdjustment>,
    var order: MutableList<String>,
) : WeightedAccessorialAdjustmentStore {
    override fun find(code: String): WeightedAccessorialAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAccessorialAdjustment) {
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

    override fun list(): MutableList<WeightedAccessorialAdjustment> {
        val out = mutableListOf<WeightedAccessorialAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAccessorialAdjustmentStore(): WeightedAccessorialAdjustmentStore {
    return MemoryWeightedAccessorialAdjustmentStore(
        mutableMapOf<String, WeightedAccessorialAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAccessorialAdjustmentStore(
    var inner: WeightedAccessorialAdjustmentStore,
    var cache: TtlCache<WeightedAccessorialAdjustment>,
    var hits: Int,
    var misses: Int,
) : WeightedAccessorialAdjustmentStore {
    override fun find(code: String): WeightedAccessorialAdjustment? {
        val hit = (cache.get(code) as WeightedAccessorialAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAccessorialAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAccessorialAdjustment> = inner.list()
}

fun newCachedWeightedAccessorialAdjustmentStore(inner: WeightedAccessorialAdjustmentStore, ttl: Int, clock: Clock): WeightedAccessorialAdjustmentStore {
    return CachedWeightedAccessorialAdjustmentStore(
        inner = inner,
        cache = newTtlCache<WeightedAccessorialAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted accessorial adjustments. Empty fields match all. */
data class WeightedAccessorialAdjustmentQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAccessorialAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAccessorialAdjustment>): MutableList<WeightedAccessorialAdjustment> {
        val matched = filter(items) { candidate: WeightedAccessorialAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAccessorialAdjustments(store: WeightedAccessorialAdjustmentStore, query: WeightedAccessorialAdjustmentQuery): MutableList<WeightedAccessorialAdjustment> {
    return query.apply(store.list())
}
