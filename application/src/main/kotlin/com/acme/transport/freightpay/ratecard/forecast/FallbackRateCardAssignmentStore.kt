package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rate card assignments by code. */
interface FallbackRateCardAssignmentStore {
    fun find(code: String): FallbackRateCardAssignment?
    fun save(item: FallbackRateCardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRateCardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRateCardAssignmentStore(
    var items: MutableMap<String, FallbackRateCardAssignment>,
    var order: MutableList<String>,
) : FallbackRateCardAssignmentStore {
    override fun find(code: String): FallbackRateCardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRateCardAssignment) {
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

    override fun list(): MutableList<FallbackRateCardAssignment> {
        val out = mutableListOf<FallbackRateCardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRateCardAssignmentStore(): FallbackRateCardAssignmentStore {
    return MemoryFallbackRateCardAssignmentStore(
        mutableMapOf<String, FallbackRateCardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRateCardAssignmentStore(
    var inner: FallbackRateCardAssignmentStore,
    var cache: TtlCache<FallbackRateCardAssignment>,
    var hits: Int,
    var misses: Int,
) : FallbackRateCardAssignmentStore {
    override fun find(code: String): FallbackRateCardAssignment? {
        val hit = (cache.get(code) as FallbackRateCardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRateCardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRateCardAssignment> = inner.list()
}

fun newCachedFallbackRateCardAssignmentStore(inner: FallbackRateCardAssignmentStore, ttl: Int, clock: Clock): FallbackRateCardAssignmentStore {
    return CachedFallbackRateCardAssignmentStore(
        inner = inner,
        cache = newTtlCache<FallbackRateCardAssignment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rate card assignments. Empty fields match all. */
data class FallbackRateCardAssignmentQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRateCardAssignment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FallbackRateCardAssignment>): MutableList<FallbackRateCardAssignment> {
        val matched = filter(items) { candidate: FallbackRateCardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRateCardAssignments(store: FallbackRateCardAssignmentStore, query: FallbackRateCardAssignmentQuery): MutableList<FallbackRateCardAssignment> {
    return query.apply(store.list())
}
