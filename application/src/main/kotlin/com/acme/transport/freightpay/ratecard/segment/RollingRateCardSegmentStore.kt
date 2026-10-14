package com.acme.transport.freightpay.ratecard.segment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling rate card segments by code. */
interface RollingRateCardSegmentStore {
    fun find(code: String): RollingRateCardSegment?
    fun save(item: RollingRateCardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRateCardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRateCardSegmentStore(
    var items: MutableMap<String, RollingRateCardSegment>,
    var order: MutableList<String>,
) : RollingRateCardSegmentStore {
    override fun find(code: String): RollingRateCardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRateCardSegment) {
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

    override fun list(): MutableList<RollingRateCardSegment> {
        val out = mutableListOf<RollingRateCardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRateCardSegmentStore(): RollingRateCardSegmentStore {
    return MemoryRollingRateCardSegmentStore(
        mutableMapOf<String, RollingRateCardSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRateCardSegmentStore(
    var inner: RollingRateCardSegmentStore,
    var cache: TtlCache<RollingRateCardSegment>,
    var hits: Int,
    var misses: Int,
) : RollingRateCardSegmentStore {
    override fun find(code: String): RollingRateCardSegment? {
        val hit = (cache.get(code) as RollingRateCardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRateCardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRateCardSegment> = inner.list()
}

fun newCachedRollingRateCardSegmentStore(inner: RollingRateCardSegmentStore, ttl: Int, clock: Clock): RollingRateCardSegmentStore {
    return CachedRollingRateCardSegmentStore(
        inner = inner,
        cache = newTtlCache<RollingRateCardSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling rate card segments. Empty fields match all. */
data class RollingRateCardSegmentQuery(
    var segment: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRateCardSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRateCardSegment>): MutableList<RollingRateCardSegment> {
        val matched = filter(items) { candidate: RollingRateCardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRateCardSegments(store: RollingRateCardSegmentStore, query: RollingRateCardSegmentQuery): MutableList<RollingRateCardSegment> {
    return query.apply(store.list())
}
