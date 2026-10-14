package com.acme.transport.freightpay.fuelsurcharge.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient fuel surcharge assignments by code. */
interface LenientFuelSurchargeAssignmentStore {
    fun find(code: String): LenientFuelSurchargeAssignment?
    fun save(item: LenientFuelSurchargeAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFuelSurchargeAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFuelSurchargeAssignmentStore(
    var items: MutableMap<String, LenientFuelSurchargeAssignment>,
    var order: MutableList<String>,
) : LenientFuelSurchargeAssignmentStore {
    override fun find(code: String): LenientFuelSurchargeAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFuelSurchargeAssignment) {
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

    override fun list(): MutableList<LenientFuelSurchargeAssignment> {
        val out = mutableListOf<LenientFuelSurchargeAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFuelSurchargeAssignmentStore(): LenientFuelSurchargeAssignmentStore {
    return MemoryLenientFuelSurchargeAssignmentStore(
        mutableMapOf<String, LenientFuelSurchargeAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFuelSurchargeAssignmentStore(
    var inner: LenientFuelSurchargeAssignmentStore,
    var cache: TtlCache<LenientFuelSurchargeAssignment>,
    var hits: Int,
    var misses: Int,
) : LenientFuelSurchargeAssignmentStore {
    override fun find(code: String): LenientFuelSurchargeAssignment? {
        val hit = (cache.get(code) as LenientFuelSurchargeAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFuelSurchargeAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFuelSurchargeAssignment> = inner.list()
}

fun newCachedLenientFuelSurchargeAssignmentStore(inner: LenientFuelSurchargeAssignmentStore, ttl: Int, clock: Clock): LenientFuelSurchargeAssignmentStore {
    return CachedLenientFuelSurchargeAssignmentStore(
        inner = inner,
        cache = newTtlCache<LenientFuelSurchargeAssignment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient fuel surcharge assignments. Empty fields match all. */
data class LenientFuelSurchargeAssignmentQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFuelSurchargeAssignment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFuelSurchargeAssignment>): MutableList<LenientFuelSurchargeAssignment> {
        val matched = filter(items) { candidate: LenientFuelSurchargeAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFuelSurchargeAssignments(store: LenientFuelSurchargeAssignmentStore, query: LenientFuelSurchargeAssignmentQuery): MutableList<LenientFuelSurchargeAssignment> {
    return query.apply(store.list())
}
