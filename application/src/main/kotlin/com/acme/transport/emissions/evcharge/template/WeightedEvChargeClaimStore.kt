package com.acme.transport.emissions.evcharge.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted ev charge claims by code. */
interface WeightedEvChargeClaimStore {
    fun find(code: String): WeightedEvChargeClaim?
    fun save(item: WeightedEvChargeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEvChargeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEvChargeClaimStore(
    var items: MutableMap<String, WeightedEvChargeClaim>,
    var order: MutableList<String>,
) : WeightedEvChargeClaimStore {
    override fun find(code: String): WeightedEvChargeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEvChargeClaim) {
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

    override fun list(): MutableList<WeightedEvChargeClaim> {
        val out = mutableListOf<WeightedEvChargeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEvChargeClaimStore(): WeightedEvChargeClaimStore {
    return MemoryWeightedEvChargeClaimStore(
        mutableMapOf<String, WeightedEvChargeClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEvChargeClaimStore(
    var inner: WeightedEvChargeClaimStore,
    var cache: TtlCache<WeightedEvChargeClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedEvChargeClaimStore {
    override fun find(code: String): WeightedEvChargeClaim? {
        val hit = (cache.get(code) as WeightedEvChargeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEvChargeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEvChargeClaim> = inner.list()
}

fun newCachedWeightedEvChargeClaimStore(inner: WeightedEvChargeClaimStore, ttl: Int, clock: Clock): WeightedEvChargeClaimStore {
    return CachedWeightedEvChargeClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedEvChargeClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted ev charge claims. Empty fields match all. */
data class WeightedEvChargeClaimQuery(
    var region: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEvChargeClaim): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<WeightedEvChargeClaim>): MutableList<WeightedEvChargeClaim> {
        val matched = filter(items) { candidate: WeightedEvChargeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEvChargeClaims(store: WeightedEvChargeClaimStore, query: WeightedEvChargeClaimQuery): MutableList<WeightedEvChargeClaim> {
    return query.apply(store.list())
}
