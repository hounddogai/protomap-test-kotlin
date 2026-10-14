package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn requests by code. */
interface FuelBurnRequestStore {
    fun find(code: String): FuelBurnRequest?
    fun save(item: FuelBurnRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnRequestStore(
    var items: MutableMap<String, FuelBurnRequest>,
    var order: MutableList<String>,
) : FuelBurnRequestStore {
    override fun find(code: String): FuelBurnRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnRequest) {
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

    override fun list(): MutableList<FuelBurnRequest> {
        val out = mutableListOf<FuelBurnRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnRequestStore(): FuelBurnRequestStore {
    return MemoryFuelBurnRequestStore(mutableMapOf<String, FuelBurnRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnRequestStore(
    var inner: FuelBurnRequestStore,
    var cache: TtlCache<FuelBurnRequest>,
    var hits: Int,
    var misses: Int,
) : FuelBurnRequestStore {
    override fun find(code: String): FuelBurnRequest? {
        val hit = (cache.get(code) as FuelBurnRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnRequest> = inner.list()
}

fun newCachedFuelBurnRequestStore(inner: FuelBurnRequestStore, ttl: Int, clock: Clock): FuelBurnRequestStore {
    return CachedFuelBurnRequestStore(
        inner = inner,
        cache = newTtlCache<FuelBurnRequest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn requests. Empty fields match all. */
data class FuelBurnRequestQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnRequest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<FuelBurnRequest>): MutableList<FuelBurnRequest> {
        val matched = filter(items) { candidate: FuelBurnRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnRequests(store: FuelBurnRequestStore, query: FuelBurnRequestQuery): MutableList<FuelBurnRequest> {
    return query.apply(store.list())
}
