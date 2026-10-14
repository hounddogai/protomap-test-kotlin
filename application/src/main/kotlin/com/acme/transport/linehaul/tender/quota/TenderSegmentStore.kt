package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender segments by code. */
interface TenderSegmentStore {
    fun find(code: String): TenderSegment?
    fun save(item: TenderSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderSegmentStore(
    var items: MutableMap<String, TenderSegment>,
    var order: MutableList<String>,
) : TenderSegmentStore {
    override fun find(code: String): TenderSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderSegment) {
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

    override fun list(): MutableList<TenderSegment> {
        val out = mutableListOf<TenderSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderSegmentStore(): TenderSegmentStore {
    return MemoryTenderSegmentStore(mutableMapOf<String, TenderSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderSegmentStore(
    var inner: TenderSegmentStore,
    var cache: TtlCache<TenderSegment>,
    var hits: Int,
    var misses: Int,
) : TenderSegmentStore {
    override fun find(code: String): TenderSegment? {
        val hit = (cache.get(code) as TenderSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderSegment> = inner.list()
}

fun newCachedTenderSegmentStore(inner: TenderSegmentStore, ttl: Int, clock: Clock): TenderSegmentStore {
    return CachedTenderSegmentStore(
        inner = inner,
        cache = newTtlCache<TenderSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender segments. Empty fields match all. */
data class TenderSegmentQuery(
    var segment: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TenderSegment>): MutableList<TenderSegment> {
        val matched = filter(items) { candidate: TenderSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderSegments(store: TenderSegmentStore, query: TenderSegmentQuery): MutableList<TenderSegment> {
    return query.apply(store.list())
}
