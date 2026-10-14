package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge assignments by code. */
interface FuelSurchargeAssignmentStore {
    fun find(code: String): FuelSurchargeAssignment?
    fun save(item: FuelSurchargeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeAssignmentStore(
    var items: MutableMap<String, FuelSurchargeAssignment>,
    var order: MutableList<String>,
) : FuelSurchargeAssignmentStore {
    override fun find(code: String): FuelSurchargeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeAssignment) {
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

    override fun list(): MutableList<FuelSurchargeAssignment> {
        val out = mutableListOf<FuelSurchargeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeAssignmentStore(): FuelSurchargeAssignmentStore {
    return MemoryFuelSurchargeAssignmentStore(
        mutableMapOf<String, FuelSurchargeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeAssignmentStore(
    var inner: FuelSurchargeAssignmentStore,
    var cache: TtlCache<FuelSurchargeAssignment>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeAssignmentStore {
    override fun find(code: String): FuelSurchargeAssignment? {
        val hit = (cache.get(code) as FuelSurchargeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeAssignment> = inner.list()
}

fun newCachedFuelSurchargeAssignmentStore(inner: FuelSurchargeAssignmentStore, ttl: Int, clock: Clock): FuelSurchargeAssignmentStore {
    return CachedFuelSurchargeAssignmentStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge assignments. Empty fields match all. */
data class FuelSurchargeAssignmentQuery(
    var region: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeAssignment): Boolean {
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

    fun apply(items: MutableList<FuelSurchargeAssignment>): MutableList<FuelSurchargeAssignment> {
        val matched = filter(items) { candidate: FuelSurchargeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeAssignments(store: FuelSurchargeAssignmentStore, query: FuelSurchargeAssignmentQuery): MutableList<FuelSurchargeAssignment> {
    return query.apply(store.list())
}
