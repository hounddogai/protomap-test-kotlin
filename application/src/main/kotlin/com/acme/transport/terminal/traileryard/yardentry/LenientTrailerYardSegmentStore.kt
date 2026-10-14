package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient trailer yard segments by code. */
interface LenientTrailerYardSegmentStore {
    fun find(code: String): LenientTrailerYardSegment?
    fun save(item: LenientTrailerYardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientTrailerYardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientTrailerYardSegmentStore(
    var items: MutableMap<String, LenientTrailerYardSegment>,
    var order: MutableList<String>,
) : LenientTrailerYardSegmentStore {
    override fun find(code: String): LenientTrailerYardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientTrailerYardSegment) {
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

    override fun list(): MutableList<LenientTrailerYardSegment> {
        val out = mutableListOf<LenientTrailerYardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientTrailerYardSegmentStore(): LenientTrailerYardSegmentStore {
    return MemoryLenientTrailerYardSegmentStore(
        mutableMapOf<String, LenientTrailerYardSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientTrailerYardSegmentStore(
    var inner: LenientTrailerYardSegmentStore,
    var cache: TtlCache<LenientTrailerYardSegment>,
    var hits: Int,
    var misses: Int,
) : LenientTrailerYardSegmentStore {
    override fun find(code: String): LenientTrailerYardSegment? {
        val hit = (cache.get(code) as LenientTrailerYardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientTrailerYardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientTrailerYardSegment> = inner.list()
}

fun newCachedLenientTrailerYardSegmentStore(inner: LenientTrailerYardSegmentStore, ttl: Int, clock: Clock): LenientTrailerYardSegmentStore {
    return CachedLenientTrailerYardSegmentStore(
        inner = inner,
        cache = newTtlCache<LenientTrailerYardSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient trailer yard segments. Empty fields match all. */
data class LenientTrailerYardSegmentQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientTrailerYardSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientTrailerYardSegment>): MutableList<LenientTrailerYardSegment> {
        val matched = filter(items) { candidate: LenientTrailerYardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientTrailerYardSegments(store: LenientTrailerYardSegmentStore, query: LenientTrailerYardSegmentQuery): MutableList<LenientTrailerYardSegment> {
    return query.apply(store.list())
}
