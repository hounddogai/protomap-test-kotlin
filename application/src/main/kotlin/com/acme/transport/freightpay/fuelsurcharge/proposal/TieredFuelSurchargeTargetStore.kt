package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel surcharge targets by code. */
interface TieredFuelSurchargeTargetStore {
    fun find(code: String): TieredFuelSurchargeTarget?
    fun save(item: TieredFuelSurchargeTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelSurchargeTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelSurchargeTargetStore(
    var items: MutableMap<String, TieredFuelSurchargeTarget>,
    var order: MutableList<String>,
) : TieredFuelSurchargeTargetStore {
    override fun find(code: String): TieredFuelSurchargeTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelSurchargeTarget) {
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

    override fun list(): MutableList<TieredFuelSurchargeTarget> {
        val out = mutableListOf<TieredFuelSurchargeTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelSurchargeTargetStore(): TieredFuelSurchargeTargetStore {
    return MemoryTieredFuelSurchargeTargetStore(
        mutableMapOf<String, TieredFuelSurchargeTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelSurchargeTargetStore(
    var inner: TieredFuelSurchargeTargetStore,
    var cache: TtlCache<TieredFuelSurchargeTarget>,
    var hits: Int,
    var misses: Int,
) : TieredFuelSurchargeTargetStore {
    override fun find(code: String): TieredFuelSurchargeTarget? {
        val hit = (cache.get(code) as TieredFuelSurchargeTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelSurchargeTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelSurchargeTarget> = inner.list()
}

fun newCachedTieredFuelSurchargeTargetStore(inner: TieredFuelSurchargeTargetStore, ttl: Int, clock: Clock): TieredFuelSurchargeTargetStore {
    return CachedTieredFuelSurchargeTargetStore(
        inner = inner,
        cache = newTtlCache<TieredFuelSurchargeTarget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel surcharge targets. Empty fields match all. */
data class TieredFuelSurchargeTargetQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelSurchargeTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<TieredFuelSurchargeTarget>): MutableList<TieredFuelSurchargeTarget> {
        val matched = filter(items) { candidate: TieredFuelSurchargeTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelSurchargeTargets(store: TieredFuelSurchargeTargetStore, query: TieredFuelSurchargeTargetQuery): MutableList<TieredFuelSurchargeTarget> {
    return query.apply(store.list())
}
