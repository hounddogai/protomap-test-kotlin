package com.acme.transport.freightpay.fuelsurcharge.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual fuel surcharge variances by code. */
interface ManualFuelSurchargeVarianceStore {
    fun find(code: String): ManualFuelSurchargeVariance?
    fun save(item: ManualFuelSurchargeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFuelSurchargeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFuelSurchargeVarianceStore(
    var items: MutableMap<String, ManualFuelSurchargeVariance>,
    var order: MutableList<String>,
) : ManualFuelSurchargeVarianceStore {
    override fun find(code: String): ManualFuelSurchargeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFuelSurchargeVariance) {
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

    override fun list(): MutableList<ManualFuelSurchargeVariance> {
        val out = mutableListOf<ManualFuelSurchargeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFuelSurchargeVarianceStore(): ManualFuelSurchargeVarianceStore {
    return MemoryManualFuelSurchargeVarianceStore(
        mutableMapOf<String, ManualFuelSurchargeVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFuelSurchargeVarianceStore(
    var inner: ManualFuelSurchargeVarianceStore,
    var cache: TtlCache<ManualFuelSurchargeVariance>,
    var hits: Int,
    var misses: Int,
) : ManualFuelSurchargeVarianceStore {
    override fun find(code: String): ManualFuelSurchargeVariance? {
        val hit = (cache.get(code) as ManualFuelSurchargeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFuelSurchargeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFuelSurchargeVariance> = inner.list()
}

fun newCachedManualFuelSurchargeVarianceStore(inner: ManualFuelSurchargeVarianceStore, ttl: Int, clock: Clock): ManualFuelSurchargeVarianceStore {
    return CachedManualFuelSurchargeVarianceStore(
        inner = inner,
        cache = newTtlCache<ManualFuelSurchargeVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual fuel surcharge variances. Empty fields match all. */
data class ManualFuelSurchargeVarianceQuery(
    var lane: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFuelSurchargeVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualFuelSurchargeVariance>): MutableList<ManualFuelSurchargeVariance> {
        val matched = filter(items) { candidate: ManualFuelSurchargeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFuelSurchargeVariances(store: ManualFuelSurchargeVarianceStore, query: ManualFuelSurchargeVarianceQuery): MutableList<ManualFuelSurchargeVariance> {
    return query.apply(store.list())
}
