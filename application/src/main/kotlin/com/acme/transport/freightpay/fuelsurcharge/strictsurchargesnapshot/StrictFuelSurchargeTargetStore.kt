package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict fuel surcharge targets by code. */
interface StrictFuelSurchargeTargetStore {
    fun find(code: String): StrictFuelSurchargeTarget?
    fun save(item: StrictFuelSurchargeTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictFuelSurchargeTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictFuelSurchargeTargetStore(
    var items: MutableMap<String, StrictFuelSurchargeTarget>,
    var order: MutableList<String>,
) : StrictFuelSurchargeTargetStore {
    override fun find(code: String): StrictFuelSurchargeTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictFuelSurchargeTarget) {
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

    override fun list(): MutableList<StrictFuelSurchargeTarget> {
        val out = mutableListOf<StrictFuelSurchargeTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictFuelSurchargeTargetStore(): StrictFuelSurchargeTargetStore {
    return MemoryStrictFuelSurchargeTargetStore(
        mutableMapOf<String, StrictFuelSurchargeTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictFuelSurchargeTargetStore(
    var inner: StrictFuelSurchargeTargetStore,
    var cache: TtlCache<StrictFuelSurchargeTarget>,
    var hits: Int,
    var misses: Int,
) : StrictFuelSurchargeTargetStore {
    override fun find(code: String): StrictFuelSurchargeTarget? {
        val hit = (cache.get(code) as StrictFuelSurchargeTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictFuelSurchargeTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictFuelSurchargeTarget> = inner.list()
}

fun newCachedStrictFuelSurchargeTargetStore(inner: StrictFuelSurchargeTargetStore, ttl: Int, clock: Clock): StrictFuelSurchargeTargetStore {
    return CachedStrictFuelSurchargeTargetStore(
        inner = inner,
        cache = newTtlCache<StrictFuelSurchargeTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict fuel surcharge targets. Empty fields match all. */
data class StrictFuelSurchargeTargetQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictFuelSurchargeTarget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictFuelSurchargeTarget>): MutableList<StrictFuelSurchargeTarget> {
        val matched = filter(items) { candidate: StrictFuelSurchargeTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictFuelSurchargeTargets(store: StrictFuelSurchargeTargetStore, query: StrictFuelSurchargeTargetQuery): MutableList<StrictFuelSurchargeTarget> {
    return query.apply(store.list())
}
