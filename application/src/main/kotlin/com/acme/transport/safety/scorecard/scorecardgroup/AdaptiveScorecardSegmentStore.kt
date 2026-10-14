package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive scorecard segments by code. */
interface AdaptiveScorecardSegmentStore {
    fun find(code: String): AdaptiveScorecardSegment?
    fun save(item: AdaptiveScorecardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveScorecardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveScorecardSegmentStore(
    var items: MutableMap<String, AdaptiveScorecardSegment>,
    var order: MutableList<String>,
) : AdaptiveScorecardSegmentStore {
    override fun find(code: String): AdaptiveScorecardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveScorecardSegment) {
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

    override fun list(): MutableList<AdaptiveScorecardSegment> {
        val out = mutableListOf<AdaptiveScorecardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveScorecardSegmentStore(): AdaptiveScorecardSegmentStore {
    return MemoryAdaptiveScorecardSegmentStore(
        mutableMapOf<String, AdaptiveScorecardSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveScorecardSegmentStore(
    var inner: AdaptiveScorecardSegmentStore,
    var cache: TtlCache<AdaptiveScorecardSegment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveScorecardSegmentStore {
    override fun find(code: String): AdaptiveScorecardSegment? {
        val hit = (cache.get(code) as AdaptiveScorecardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveScorecardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveScorecardSegment> = inner.list()
}

fun newCachedAdaptiveScorecardSegmentStore(inner: AdaptiveScorecardSegmentStore, ttl: Int, clock: Clock): AdaptiveScorecardSegmentStore {
    return CachedAdaptiveScorecardSegmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveScorecardSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive scorecard segments. Empty fields match all. */
data class AdaptiveScorecardSegmentQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveScorecardSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveScorecardSegment>): MutableList<AdaptiveScorecardSegment> {
        val matched = filter(items) { candidate: AdaptiveScorecardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveScorecardSegments(store: AdaptiveScorecardSegmentStore, query: AdaptiveScorecardSegmentQuery): MutableList<AdaptiveScorecardSegment> {
    return query.apply(store.list())
}
