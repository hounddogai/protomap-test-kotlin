package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive idling segments by code. */
interface AdaptiveIdlingSegmentStore {
    fun find(code: String): AdaptiveIdlingSegment?
    fun save(item: AdaptiveIdlingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveIdlingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveIdlingSegmentStore(
    var items: MutableMap<String, AdaptiveIdlingSegment>,
    var order: MutableList<String>,
) : AdaptiveIdlingSegmentStore {
    override fun find(code: String): AdaptiveIdlingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveIdlingSegment) {
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

    override fun list(): MutableList<AdaptiveIdlingSegment> {
        val out = mutableListOf<AdaptiveIdlingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveIdlingSegmentStore(): AdaptiveIdlingSegmentStore {
    return MemoryAdaptiveIdlingSegmentStore(
        mutableMapOf<String, AdaptiveIdlingSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveIdlingSegmentStore(
    var inner: AdaptiveIdlingSegmentStore,
    var cache: TtlCache<AdaptiveIdlingSegment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveIdlingSegmentStore {
    override fun find(code: String): AdaptiveIdlingSegment? {
        val hit = (cache.get(code) as AdaptiveIdlingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveIdlingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveIdlingSegment> = inner.list()
}

fun newCachedAdaptiveIdlingSegmentStore(inner: AdaptiveIdlingSegmentStore, ttl: Int, clock: Clock): AdaptiveIdlingSegmentStore {
    return CachedAdaptiveIdlingSegmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveIdlingSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive idling segments. Empty fields match all. */
data class AdaptiveIdlingSegmentQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveIdlingSegment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveIdlingSegment>): MutableList<AdaptiveIdlingSegment> {
        val matched = filter(items) { candidate: AdaptiveIdlingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveIdlingSegments(store: AdaptiveIdlingSegmentStore, query: AdaptiveIdlingSegmentQuery): MutableList<AdaptiveIdlingSegment> {
    return query.apply(store.list())
}
