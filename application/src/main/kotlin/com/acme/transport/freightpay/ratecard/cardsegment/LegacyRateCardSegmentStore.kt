package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy rate card segments by code. */
interface LegacyRateCardSegmentStore {
    fun find(code: String): LegacyRateCardSegment?
    fun save(item: LegacyRateCardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyRateCardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyRateCardSegmentStore(
    var items: MutableMap<String, LegacyRateCardSegment>,
    var order: MutableList<String>,
) : LegacyRateCardSegmentStore {
    override fun find(code: String): LegacyRateCardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyRateCardSegment) {
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

    override fun list(): MutableList<LegacyRateCardSegment> {
        val out = mutableListOf<LegacyRateCardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyRateCardSegmentStore(): LegacyRateCardSegmentStore {
    return MemoryLegacyRateCardSegmentStore(
        mutableMapOf<String, LegacyRateCardSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyRateCardSegmentStore(
    var inner: LegacyRateCardSegmentStore,
    var cache: TtlCache<LegacyRateCardSegment>,
    var hits: Int,
    var misses: Int,
) : LegacyRateCardSegmentStore {
    override fun find(code: String): LegacyRateCardSegment? {
        val hit = (cache.get(code) as LegacyRateCardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyRateCardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyRateCardSegment> = inner.list()
}

fun newCachedLegacyRateCardSegmentStore(inner: LegacyRateCardSegmentStore, ttl: Int, clock: Clock): LegacyRateCardSegmentStore {
    return CachedLegacyRateCardSegmentStore(
        inner = inner,
        cache = newTtlCache<LegacyRateCardSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy rate card segments. Empty fields match all. */
data class LegacyRateCardSegmentQuery(
    var segment: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyRateCardSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyRateCardSegment>): MutableList<LegacyRateCardSegment> {
        val matched = filter(items) { candidate: LegacyRateCardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyRateCardSegments(store: LegacyRateCardSegmentStore, query: LegacyRateCardSegmentQuery): MutableList<LegacyRateCardSegment> {
    return query.apply(store.list())
}
