package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel surcharge adjustments by code. */
interface TieredFuelSurchargeAdjustmentStore {
    fun find(code: String): TieredFuelSurchargeAdjustment?
    fun save(item: TieredFuelSurchargeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelSurchargeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelSurchargeAdjustmentStore(
    var items: MutableMap<String, TieredFuelSurchargeAdjustment>,
    var order: MutableList<String>,
) : TieredFuelSurchargeAdjustmentStore {
    override fun find(code: String): TieredFuelSurchargeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelSurchargeAdjustment) {
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

    override fun list(): MutableList<TieredFuelSurchargeAdjustment> {
        val out = mutableListOf<TieredFuelSurchargeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelSurchargeAdjustmentStore(): TieredFuelSurchargeAdjustmentStore {
    return MemoryTieredFuelSurchargeAdjustmentStore(
        mutableMapOf<String, TieredFuelSurchargeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelSurchargeAdjustmentStore(
    var inner: TieredFuelSurchargeAdjustmentStore,
    var cache: TtlCache<TieredFuelSurchargeAdjustment>,
    var hits: Int,
    var misses: Int,
) : TieredFuelSurchargeAdjustmentStore {
    override fun find(code: String): TieredFuelSurchargeAdjustment? {
        val hit = (cache.get(code) as TieredFuelSurchargeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelSurchargeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelSurchargeAdjustment> = inner.list()
}

fun newCachedTieredFuelSurchargeAdjustmentStore(inner: TieredFuelSurchargeAdjustmentStore, ttl: Int, clock: Clock): TieredFuelSurchargeAdjustmentStore {
    return CachedTieredFuelSurchargeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TieredFuelSurchargeAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel surcharge adjustments. Empty fields match all. */
data class TieredFuelSurchargeAdjustmentQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelSurchargeAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredFuelSurchargeAdjustment>): MutableList<TieredFuelSurchargeAdjustment> {
        val matched = filter(items) { candidate: TieredFuelSurchargeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelSurchargeAdjustments(store: TieredFuelSurchargeAdjustmentStore, query: TieredFuelSurchargeAdjustmentQuery): MutableList<TieredFuelSurchargeAdjustment> {
    return query.apply(store.list())
}
