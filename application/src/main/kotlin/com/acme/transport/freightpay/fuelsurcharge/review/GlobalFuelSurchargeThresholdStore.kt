package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel surcharge thresholds by code. */
interface GlobalFuelSurchargeThresholdStore {
    fun find(code: String): GlobalFuelSurchargeThreshold?
    fun save(item: GlobalFuelSurchargeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelSurchargeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelSurchargeThresholdStore(
    var items: MutableMap<String, GlobalFuelSurchargeThreshold>,
    var order: MutableList<String>,
) : GlobalFuelSurchargeThresholdStore {
    override fun find(code: String): GlobalFuelSurchargeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelSurchargeThreshold) {
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

    override fun list(): MutableList<GlobalFuelSurchargeThreshold> {
        val out = mutableListOf<GlobalFuelSurchargeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelSurchargeThresholdStore(): GlobalFuelSurchargeThresholdStore {
    return MemoryGlobalFuelSurchargeThresholdStore(
        mutableMapOf<String, GlobalFuelSurchargeThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelSurchargeThresholdStore(
    var inner: GlobalFuelSurchargeThresholdStore,
    var cache: TtlCache<GlobalFuelSurchargeThreshold>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelSurchargeThresholdStore {
    override fun find(code: String): GlobalFuelSurchargeThreshold? {
        val hit = (cache.get(code) as GlobalFuelSurchargeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelSurchargeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelSurchargeThreshold> = inner.list()
}

fun newCachedGlobalFuelSurchargeThresholdStore(inner: GlobalFuelSurchargeThresholdStore, ttl: Int, clock: Clock): GlobalFuelSurchargeThresholdStore {
    return CachedGlobalFuelSurchargeThresholdStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelSurchargeThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel surcharge thresholds. Empty fields match all. */
data class GlobalFuelSurchargeThresholdQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelSurchargeThreshold): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFuelSurchargeThreshold>): MutableList<GlobalFuelSurchargeThreshold> {
        val matched = filter(items) { candidate: GlobalFuelSurchargeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelSurchargeThresholds(store: GlobalFuelSurchargeThresholdStore, query: GlobalFuelSurchargeThresholdQuery): MutableList<GlobalFuelSurchargeThreshold> {
    return query.apply(store.list())
}
