package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report segments by code. */
interface CarbonReportSegmentStore {
    fun find(code: String): CarbonReportSegment?
    fun save(item: CarbonReportSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportSegmentStore(
    var items: MutableMap<String, CarbonReportSegment>,
    var order: MutableList<String>,
) : CarbonReportSegmentStore {
    override fun find(code: String): CarbonReportSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportSegment) {
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

    override fun list(): MutableList<CarbonReportSegment> {
        val out = mutableListOf<CarbonReportSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportSegmentStore(): CarbonReportSegmentStore {
    return MemoryCarbonReportSegmentStore(mutableMapOf<String, CarbonReportSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportSegmentStore(
    var inner: CarbonReportSegmentStore,
    var cache: TtlCache<CarbonReportSegment>,
    var hits: Int,
    var misses: Int,
) : CarbonReportSegmentStore {
    override fun find(code: String): CarbonReportSegment? {
        val hit = (cache.get(code) as CarbonReportSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportSegment> = inner.list()
}

fun newCachedCarbonReportSegmentStore(inner: CarbonReportSegmentStore, ttl: Int, clock: Clock): CarbonReportSegmentStore {
    return CachedCarbonReportSegmentStore(
        inner = inner,
        cache = newTtlCache<CarbonReportSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report segments. Empty fields match all. */
data class CarbonReportSegmentQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportSegment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportSegment>): MutableList<CarbonReportSegment> {
        val matched = filter(items) { candidate: CarbonReportSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportSegments(store: CarbonReportSegmentStore, query: CarbonReportSegmentQuery): MutableList<CarbonReportSegment> {
    return query.apply(store.list())
}
