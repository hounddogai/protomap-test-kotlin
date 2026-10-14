package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge records by code. */
interface FuelSurchargeRecordStore {
    fun find(code: String): FuelSurchargeRecord?
    fun save(item: FuelSurchargeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeRecordStore(
    var items: MutableMap<String, FuelSurchargeRecord>,
    var order: MutableList<String>,
) : FuelSurchargeRecordStore {
    override fun find(code: String): FuelSurchargeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeRecord) {
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

    override fun list(): MutableList<FuelSurchargeRecord> {
        val out = mutableListOf<FuelSurchargeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeRecordStore(): FuelSurchargeRecordStore {
    return MemoryFuelSurchargeRecordStore(mutableMapOf<String, FuelSurchargeRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeRecordStore(
    var inner: FuelSurchargeRecordStore,
    var cache: TtlCache<FuelSurchargeRecord>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeRecordStore {
    override fun find(code: String): FuelSurchargeRecord? {
        val hit = (cache.get(code) as FuelSurchargeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeRecord> = inner.list()
}

fun newCachedFuelSurchargeRecordStore(inner: FuelSurchargeRecordStore, ttl: Int, clock: Clock): FuelSurchargeRecordStore {
    return CachedFuelSurchargeRecordStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge records. Empty fields match all. */
data class FuelSurchargeRecordQuery(
    var depot: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeRecord): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeRecord>): MutableList<FuelSurchargeRecord> {
        val matched = filter(items) { candidate: FuelSurchargeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeRecords(store: FuelSurchargeRecordStore, query: FuelSurchargeRecordQuery): MutableList<FuelSurchargeRecord> {
    return query.apply(store.list())
}
