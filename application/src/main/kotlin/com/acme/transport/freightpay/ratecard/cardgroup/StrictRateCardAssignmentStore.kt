package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rate card assignments by code. */
interface StrictRateCardAssignmentStore {
    fun find(code: String): StrictRateCardAssignment?
    fun save(item: StrictRateCardAssignment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRateCardAssignment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRateCardAssignmentStore(
    var items: MutableMap<String, StrictRateCardAssignment>,
    var order: MutableList<String>,
) : StrictRateCardAssignmentStore {
    override fun find(code: String): StrictRateCardAssignment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRateCardAssignment) {
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

    override fun list(): MutableList<StrictRateCardAssignment> {
        val out = mutableListOf<StrictRateCardAssignment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRateCardAssignmentStore(): StrictRateCardAssignmentStore {
    return MemoryStrictRateCardAssignmentStore(
        mutableMapOf<String, StrictRateCardAssignment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRateCardAssignmentStore(
    var inner: StrictRateCardAssignmentStore,
    var cache: TtlCache<StrictRateCardAssignment>,
    var hits: Int,
    var misses: Int,
) : StrictRateCardAssignmentStore {
    override fun find(code: String): StrictRateCardAssignment? {
        val hit = (cache.get(code) as StrictRateCardAssignment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRateCardAssignment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRateCardAssignment> = inner.list()
}

fun newCachedStrictRateCardAssignmentStore(inner: StrictRateCardAssignmentStore, ttl: Int, clock: Clock): StrictRateCardAssignmentStore {
    return CachedStrictRateCardAssignmentStore(
        inner = inner,
        cache = newTtlCache<StrictRateCardAssignment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rate card assignments. Empty fields match all. */
data class StrictRateCardAssignmentQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRateCardAssignment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRateCardAssignment>): MutableList<StrictRateCardAssignment> {
        val matched = filter(items) { candidate: StrictRateCardAssignment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRateCardAssignments(store: StrictRateCardAssignmentStore, query: StrictRateCardAssignmentQuery): MutableList<StrictRateCardAssignment> {
    return query.apply(store.list())
}
