package com.acme.transport.freightpay.fuelsurcharge.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge entries by code. */
interface FuelSurchargeEntryStore {
    fun find(code: String): FuelSurchargeEntry?
    fun save(item: FuelSurchargeEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeEntryStore(
    var items: MutableMap<String, FuelSurchargeEntry>,
    var order: MutableList<String>,
) : FuelSurchargeEntryStore {
    override fun find(code: String): FuelSurchargeEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeEntry) {
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

    override fun list(): MutableList<FuelSurchargeEntry> {
        val out = mutableListOf<FuelSurchargeEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeEntryStore(): FuelSurchargeEntryStore {
    return MemoryFuelSurchargeEntryStore(mutableMapOf<String, FuelSurchargeEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeEntryStore(
    var inner: FuelSurchargeEntryStore,
    var cache: TtlCache<FuelSurchargeEntry>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeEntryStore {
    override fun find(code: String): FuelSurchargeEntry? {
        val hit = (cache.get(code) as FuelSurchargeEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeEntry> = inner.list()
}

fun newCachedFuelSurchargeEntryStore(inner: FuelSurchargeEntryStore, ttl: Int, clock: Clock): FuelSurchargeEntryStore {
    return CachedFuelSurchargeEntryStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge entries. Empty fields match all. */
data class FuelSurchargeEntryQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeEntry>): MutableList<FuelSurchargeEntry> {
        val matched = filter(items) { candidate: FuelSurchargeEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeEntries(store: FuelSurchargeEntryStore, query: FuelSurchargeEntryQuery): MutableList<FuelSurchargeEntry> {
    return query.apply(store.list())
}
