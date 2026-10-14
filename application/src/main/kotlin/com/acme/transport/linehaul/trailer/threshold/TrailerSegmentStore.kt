package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer segments by code. */
interface TrailerSegmentStore {
    fun find(code: String): TrailerSegment?
    fun save(item: TrailerSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerSegmentStore(
    var items: MutableMap<String, TrailerSegment>,
    var order: MutableList<String>,
) : TrailerSegmentStore {
    override fun find(code: String): TrailerSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerSegment) {
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

    override fun list(): MutableList<TrailerSegment> {
        val out = mutableListOf<TrailerSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerSegmentStore(): TrailerSegmentStore {
    return MemoryTrailerSegmentStore(mutableMapOf<String, TrailerSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerSegmentStore(
    var inner: TrailerSegmentStore,
    var cache: TtlCache<TrailerSegment>,
    var hits: Int,
    var misses: Int,
) : TrailerSegmentStore {
    override fun find(code: String): TrailerSegment? {
        val hit = (cache.get(code) as TrailerSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerSegment> = inner.list()
}

fun newCachedTrailerSegmentStore(inner: TrailerSegmentStore, ttl: Int, clock: Clock): TrailerSegmentStore {
    return CachedTrailerSegmentStore(
        inner = inner,
        cache = newTtlCache<TrailerSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer segments. Empty fields match all. */
data class TrailerSegmentQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerSegment>): MutableList<TrailerSegment> {
        val matched = filter(items) { candidate: TrailerSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerSegments(store: TrailerSegmentStore, query: TrailerSegmentQuery): MutableList<TrailerSegment> {
    return query.apply(store.list())
}
