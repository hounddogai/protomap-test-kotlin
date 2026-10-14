package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge baselines by code. */
interface FuelSurchargeBaselineStore {
    fun find(code: String): FuelSurchargeBaseline?
    fun save(item: FuelSurchargeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeBaselineStore(
    var items: MutableMap<String, FuelSurchargeBaseline>,
    var order: MutableList<String>,
) : FuelSurchargeBaselineStore {
    override fun find(code: String): FuelSurchargeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeBaseline) {
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

    override fun list(): MutableList<FuelSurchargeBaseline> {
        val out = mutableListOf<FuelSurchargeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeBaselineStore(): FuelSurchargeBaselineStore {
    return MemoryFuelSurchargeBaselineStore(
        mutableMapOf<String, FuelSurchargeBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeBaselineStore(
    var inner: FuelSurchargeBaselineStore,
    var cache: TtlCache<FuelSurchargeBaseline>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeBaselineStore {
    override fun find(code: String): FuelSurchargeBaseline? {
        val hit = (cache.get(code) as FuelSurchargeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeBaseline> = inner.list()
}

fun newCachedFuelSurchargeBaselineStore(inner: FuelSurchargeBaselineStore, ttl: Int, clock: Clock): FuelSurchargeBaselineStore {
    return CachedFuelSurchargeBaselineStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge baselines. Empty fields match all. */
data class FuelSurchargeBaselineQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeBaseline>): MutableList<FuelSurchargeBaseline> {
        val matched = filter(items) { candidate: FuelSurchargeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeBaselines(store: FuelSurchargeBaselineStore, query: FuelSurchargeBaselineQuery): MutableList<FuelSurchargeBaseline> {
    return query.apply(store.list())
}
