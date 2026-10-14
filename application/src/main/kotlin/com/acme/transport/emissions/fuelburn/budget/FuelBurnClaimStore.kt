package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn claims by code. */
interface FuelBurnClaimStore {
    fun find(code: String): FuelBurnClaim?
    fun save(item: FuelBurnClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnClaimStore(
    var items: MutableMap<String, FuelBurnClaim>,
    var order: MutableList<String>,
) : FuelBurnClaimStore {
    override fun find(code: String): FuelBurnClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnClaim) {
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

    override fun list(): MutableList<FuelBurnClaim> {
        val out = mutableListOf<FuelBurnClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnClaimStore(): FuelBurnClaimStore {
    return MemoryFuelBurnClaimStore(mutableMapOf<String, FuelBurnClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnClaimStore(
    var inner: FuelBurnClaimStore,
    var cache: TtlCache<FuelBurnClaim>,
    var hits: Int,
    var misses: Int,
) : FuelBurnClaimStore {
    override fun find(code: String): FuelBurnClaim? {
        val hit = (cache.get(code) as FuelBurnClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnClaim> = inner.list()
}

fun newCachedFuelBurnClaimStore(inner: FuelBurnClaimStore, ttl: Int, clock: Clock): FuelBurnClaimStore {
    return CachedFuelBurnClaimStore(
        inner = inner,
        cache = newTtlCache<FuelBurnClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn claims. Empty fields match all. */
data class FuelBurnClaimQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<FuelBurnClaim>): MutableList<FuelBurnClaim> {
        val matched = filter(items) { candidate: FuelBurnClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnClaims(store: FuelBurnClaimStore, query: FuelBurnClaimQuery): MutableList<FuelBurnClaim> {
    return query.apply(store.list())
}
