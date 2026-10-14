package com.acme.transport.freightpay.fuelsurcharge.exception

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge segments by code. */
interface FuelSurchargeSegmentStore {
    fun find(code: String): FuelSurchargeSegment?
    fun save(item: FuelSurchargeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeSegmentStore(
    var items: MutableMap<String, FuelSurchargeSegment>,
    var order: MutableList<String>,
) : FuelSurchargeSegmentStore {
    override fun find(code: String): FuelSurchargeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeSegment) {
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

    override fun list(): MutableList<FuelSurchargeSegment> {
        val out = mutableListOf<FuelSurchargeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeSegmentStore(): FuelSurchargeSegmentStore {
    return MemoryFuelSurchargeSegmentStore(
        mutableMapOf<String, FuelSurchargeSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeSegmentStore(
    var inner: FuelSurchargeSegmentStore,
    var cache: TtlCache<FuelSurchargeSegment>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeSegmentStore {
    override fun find(code: String): FuelSurchargeSegment? {
        val hit = (cache.get(code) as FuelSurchargeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeSegment> = inner.list()
}

fun newCachedFuelSurchargeSegmentStore(inner: FuelSurchargeSegmentStore, ttl: Int, clock: Clock): FuelSurchargeSegmentStore {
    return CachedFuelSurchargeSegmentStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge segments. Empty fields match all. */
data class FuelSurchargeSegmentQuery(
    var region: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeSegment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeSegment>): MutableList<FuelSurchargeSegment> {
        val matched = filter(items) { candidate: FuelSurchargeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeSegments(store: FuelSurchargeSegmentStore, query: FuelSurchargeSegmentQuery): MutableList<FuelSurchargeSegment> {
    return query.apply(store.list())
}
