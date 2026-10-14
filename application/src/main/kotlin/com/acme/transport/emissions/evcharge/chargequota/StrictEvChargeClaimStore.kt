package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict ev charge claims by code. */
interface StrictEvChargeClaimStore {
    fun find(code: String): StrictEvChargeClaim?
    fun save(item: StrictEvChargeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictEvChargeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictEvChargeClaimStore(
    var items: MutableMap<String, StrictEvChargeClaim>,
    var order: MutableList<String>,
) : StrictEvChargeClaimStore {
    override fun find(code: String): StrictEvChargeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictEvChargeClaim) {
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

    override fun list(): MutableList<StrictEvChargeClaim> {
        val out = mutableListOf<StrictEvChargeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictEvChargeClaimStore(): StrictEvChargeClaimStore {
    return MemoryStrictEvChargeClaimStore(mutableMapOf<String, StrictEvChargeClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictEvChargeClaimStore(
    var inner: StrictEvChargeClaimStore,
    var cache: TtlCache<StrictEvChargeClaim>,
    var hits: Int,
    var misses: Int,
) : StrictEvChargeClaimStore {
    override fun find(code: String): StrictEvChargeClaim? {
        val hit = (cache.get(code) as StrictEvChargeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictEvChargeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictEvChargeClaim> = inner.list()
}

fun newCachedStrictEvChargeClaimStore(inner: StrictEvChargeClaimStore, ttl: Int, clock: Clock): StrictEvChargeClaimStore {
    return CachedStrictEvChargeClaimStore(
        inner = inner,
        cache = newTtlCache<StrictEvChargeClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict ev charge claims. Empty fields match all. */
data class StrictEvChargeClaimQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictEvChargeClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<StrictEvChargeClaim>): MutableList<StrictEvChargeClaim> {
        val matched = filter(items) { candidate: StrictEvChargeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictEvChargeClaims(store: StrictEvChargeClaimStore, query: StrictEvChargeClaimQuery): MutableList<StrictEvChargeClaim> {
    return query.apply(store.list())
}
