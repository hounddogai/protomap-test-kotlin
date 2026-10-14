package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary fuel surcharge assignments by code. */
interface PrimaryFuelSurchargeAssignmentStore {
    fun find(code: String): PrimaryFuelSurchargeAssignment?
    fun save(item: PrimaryFuelSurchargeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFuelSurchargeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFuelSurchargeAssignmentStore(
    var items: MutableMap<String, PrimaryFuelSurchargeAssignment>,
    var order: MutableList<String>,
) : PrimaryFuelSurchargeAssignmentStore {
    override fun find(code: String): PrimaryFuelSurchargeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFuelSurchargeAssignment) {
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

    override fun list(): MutableList<PrimaryFuelSurchargeAssignment> {
        val out = mutableListOf<PrimaryFuelSurchargeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFuelSurchargeAssignmentStore(): PrimaryFuelSurchargeAssignmentStore {
    return MemoryPrimaryFuelSurchargeAssignmentStore(
        mutableMapOf<String, PrimaryFuelSurchargeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFuelSurchargeAssignmentStore(
    var inner: PrimaryFuelSurchargeAssignmentStore,
    var cache: TtlCache<PrimaryFuelSurchargeAssignment>,
    var hits: Int,
    var misses: Int,
) : PrimaryFuelSurchargeAssignmentStore {
    override fun find(code: String): PrimaryFuelSurchargeAssignment? {
        val hit = (cache.get(code) as PrimaryFuelSurchargeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFuelSurchargeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFuelSurchargeAssignment> = inner.list()
}

fun newCachedPrimaryFuelSurchargeAssignmentStore(inner: PrimaryFuelSurchargeAssignmentStore, ttl: Int, clock: Clock): PrimaryFuelSurchargeAssignmentStore {
    return CachedPrimaryFuelSurchargeAssignmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryFuelSurchargeAssignment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary fuel surcharge assignments. Empty fields match all. */
data class PrimaryFuelSurchargeAssignmentQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFuelSurchargeAssignment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFuelSurchargeAssignment>): MutableList<PrimaryFuelSurchargeAssignment> {
        val matched = filter(items) { candidate: PrimaryFuelSurchargeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFuelSurchargeAssignments(store: PrimaryFuelSurchargeAssignmentStore, query: PrimaryFuelSurchargeAssignmentQuery): MutableList<PrimaryFuelSurchargeAssignment> {
    return query.apply(store.list())
}
