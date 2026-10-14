package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict scorecard segments by code. */
interface StrictScorecardSegmentStore {
    fun find(code: String): StrictScorecardSegment?
    fun save(item: StrictScorecardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictScorecardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictScorecardSegmentStore(
    var items: MutableMap<String, StrictScorecardSegment>,
    var order: MutableList<String>,
) : StrictScorecardSegmentStore {
    override fun find(code: String): StrictScorecardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictScorecardSegment) {
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

    override fun list(): MutableList<StrictScorecardSegment> {
        val out = mutableListOf<StrictScorecardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictScorecardSegmentStore(): StrictScorecardSegmentStore {
    return MemoryStrictScorecardSegmentStore(
        mutableMapOf<String, StrictScorecardSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictScorecardSegmentStore(
    var inner: StrictScorecardSegmentStore,
    var cache: TtlCache<StrictScorecardSegment>,
    var hits: Int,
    var misses: Int,
) : StrictScorecardSegmentStore {
    override fun find(code: String): StrictScorecardSegment? {
        val hit = (cache.get(code) as StrictScorecardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictScorecardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictScorecardSegment> = inner.list()
}

fun newCachedStrictScorecardSegmentStore(inner: StrictScorecardSegmentStore, ttl: Int, clock: Clock): StrictScorecardSegmentStore {
    return CachedStrictScorecardSegmentStore(
        inner = inner,
        cache = newTtlCache<StrictScorecardSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict scorecard segments. Empty fields match all. */
data class StrictScorecardSegmentQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictScorecardSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictScorecardSegment>): MutableList<StrictScorecardSegment> {
        val matched = filter(items) { candidate: StrictScorecardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictScorecardSegments(store: StrictScorecardSegmentStore, query: StrictScorecardSegmentQuery): MutableList<StrictScorecardSegment> {
    return query.apply(store.list())
}
