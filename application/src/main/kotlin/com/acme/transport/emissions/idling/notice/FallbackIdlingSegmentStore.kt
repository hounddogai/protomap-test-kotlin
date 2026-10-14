package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback idling segments by code. */
interface FallbackIdlingSegmentStore {
    fun find(code: String): FallbackIdlingSegment?
    fun save(item: FallbackIdlingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackIdlingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackIdlingSegmentStore(
    var items: MutableMap<String, FallbackIdlingSegment>,
    var order: MutableList<String>,
) : FallbackIdlingSegmentStore {
    override fun find(code: String): FallbackIdlingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackIdlingSegment) {
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

    override fun list(): MutableList<FallbackIdlingSegment> {
        val out = mutableListOf<FallbackIdlingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackIdlingSegmentStore(): FallbackIdlingSegmentStore {
    return MemoryFallbackIdlingSegmentStore(
        mutableMapOf<String, FallbackIdlingSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackIdlingSegmentStore(
    var inner: FallbackIdlingSegmentStore,
    var cache: TtlCache<FallbackIdlingSegment>,
    var hits: Int,
    var misses: Int,
) : FallbackIdlingSegmentStore {
    override fun find(code: String): FallbackIdlingSegment? {
        val hit = (cache.get(code) as FallbackIdlingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackIdlingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackIdlingSegment> = inner.list()
}

fun newCachedFallbackIdlingSegmentStore(inner: FallbackIdlingSegmentStore, ttl: Int, clock: Clock): FallbackIdlingSegmentStore {
    return CachedFallbackIdlingSegmentStore(
        inner = inner,
        cache = newTtlCache<FallbackIdlingSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback idling segments. Empty fields match all. */
data class FallbackIdlingSegmentQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackIdlingSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackIdlingSegment>): MutableList<FallbackIdlingSegment> {
        val matched = filter(items) { candidate: FallbackIdlingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackIdlingSegments(store: FallbackIdlingSegmentStore, query: FallbackIdlingSegmentQuery): MutableList<FallbackIdlingSegment> {
    return query.apply(store.list())
}
