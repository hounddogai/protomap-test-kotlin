package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard segments by code. */
interface TrailerYardSegmentStore {
    fun find(code: String): TrailerYardSegment?
    fun save(item: TrailerYardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardSegmentStore(
    var items: MutableMap<String, TrailerYardSegment>,
    var order: MutableList<String>,
) : TrailerYardSegmentStore {
    override fun find(code: String): TrailerYardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardSegment) {
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

    override fun list(): MutableList<TrailerYardSegment> {
        val out = mutableListOf<TrailerYardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardSegmentStore(): TrailerYardSegmentStore {
    return MemoryTrailerYardSegmentStore(mutableMapOf<String, TrailerYardSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardSegmentStore(
    var inner: TrailerYardSegmentStore,
    var cache: TtlCache<TrailerYardSegment>,
    var hits: Int,
    var misses: Int,
) : TrailerYardSegmentStore {
    override fun find(code: String): TrailerYardSegment? {
        val hit = (cache.get(code) as TrailerYardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardSegment> = inner.list()
}

fun newCachedTrailerYardSegmentStore(inner: TrailerYardSegmentStore, ttl: Int, clock: Clock): TrailerYardSegmentStore {
    return CachedTrailerYardSegmentStore(
        inner = inner,
        cache = newTtlCache<TrailerYardSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard segments. Empty fields match all. */
data class TrailerYardSegmentQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TrailerYardSegment>): MutableList<TrailerYardSegment> {
        val matched = filter(items) { candidate: TrailerYardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardSegments(store: TrailerYardSegmentStore, query: TrailerYardSegmentQuery): MutableList<TrailerYardSegment> {
    return query.apply(store.list())
}
