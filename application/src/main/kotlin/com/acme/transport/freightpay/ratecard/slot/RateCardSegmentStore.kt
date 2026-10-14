package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card segments by code. */
interface RateCardSegmentStore {
    fun find(code: String): RateCardSegment?
    fun save(item: RateCardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardSegmentStore(
    var items: MutableMap<String, RateCardSegment>,
    var order: MutableList<String>,
) : RateCardSegmentStore {
    override fun find(code: String): RateCardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardSegment) {
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

    override fun list(): MutableList<RateCardSegment> {
        val out = mutableListOf<RateCardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardSegmentStore(): RateCardSegmentStore {
    return MemoryRateCardSegmentStore(mutableMapOf<String, RateCardSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardSegmentStore(
    var inner: RateCardSegmentStore,
    var cache: TtlCache<RateCardSegment>,
    var hits: Int,
    var misses: Int,
) : RateCardSegmentStore {
    override fun find(code: String): RateCardSegment? {
        val hit = (cache.get(code) as RateCardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardSegment> = inner.list()
}

fun newCachedRateCardSegmentStore(inner: RateCardSegmentStore, ttl: Int, clock: Clock): RateCardSegmentStore {
    return CachedRateCardSegmentStore(
        inner = inner,
        cache = newTtlCache<RateCardSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card segments. Empty fields match all. */
data class RateCardSegmentQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardSegment>): MutableList<RateCardSegment> {
        val matched = filter(items) { candidate: RateCardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardSegments(store: RateCardSegmentStore, query: RateCardSegmentQuery): MutableList<RateCardSegment> {
    return query.apply(store.list())
}
