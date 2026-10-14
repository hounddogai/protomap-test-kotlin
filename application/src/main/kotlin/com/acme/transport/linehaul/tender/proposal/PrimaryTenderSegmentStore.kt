package com.acme.transport.linehaul.tender.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary tender segments by code. */
interface PrimaryTenderSegmentStore {
    fun find(code: String): PrimaryTenderSegment?
    fun save(item: PrimaryTenderSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTenderSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTenderSegmentStore(
    var items: MutableMap<String, PrimaryTenderSegment>,
    var order: MutableList<String>,
) : PrimaryTenderSegmentStore {
    override fun find(code: String): PrimaryTenderSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTenderSegment) {
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

    override fun list(): MutableList<PrimaryTenderSegment> {
        val out = mutableListOf<PrimaryTenderSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTenderSegmentStore(): PrimaryTenderSegmentStore {
    return MemoryPrimaryTenderSegmentStore(
        mutableMapOf<String, PrimaryTenderSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTenderSegmentStore(
    var inner: PrimaryTenderSegmentStore,
    var cache: TtlCache<PrimaryTenderSegment>,
    var hits: Int,
    var misses: Int,
) : PrimaryTenderSegmentStore {
    override fun find(code: String): PrimaryTenderSegment? {
        val hit = (cache.get(code) as PrimaryTenderSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTenderSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTenderSegment> = inner.list()
}

fun newCachedPrimaryTenderSegmentStore(inner: PrimaryTenderSegmentStore, ttl: Int, clock: Clock): PrimaryTenderSegmentStore {
    return CachedPrimaryTenderSegmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryTenderSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tender segments. Empty fields match all. */
data class PrimaryTenderSegmentQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTenderSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTenderSegment>): MutableList<PrimaryTenderSegment> {
        val matched = filter(items) { candidate: PrimaryTenderSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTenderSegments(store: PrimaryTenderSegmentStore, query: PrimaryTenderSegmentQuery): MutableList<PrimaryTenderSegment> {
    return query.apply(store.list())
}
