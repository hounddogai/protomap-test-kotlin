package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped scorecard segments by code. */
interface CappedScorecardSegmentStore {
    fun find(code: String): CappedScorecardSegment?
    fun save(item: CappedScorecardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedScorecardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedScorecardSegmentStore(
    var items: MutableMap<String, CappedScorecardSegment>,
    var order: MutableList<String>,
) : CappedScorecardSegmentStore {
    override fun find(code: String): CappedScorecardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedScorecardSegment) {
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

    override fun list(): MutableList<CappedScorecardSegment> {
        val out = mutableListOf<CappedScorecardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedScorecardSegmentStore(): CappedScorecardSegmentStore {
    return MemoryCappedScorecardSegmentStore(
        mutableMapOf<String, CappedScorecardSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedScorecardSegmentStore(
    var inner: CappedScorecardSegmentStore,
    var cache: TtlCache<CappedScorecardSegment>,
    var hits: Int,
    var misses: Int,
) : CappedScorecardSegmentStore {
    override fun find(code: String): CappedScorecardSegment? {
        val hit = (cache.get(code) as CappedScorecardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedScorecardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedScorecardSegment> = inner.list()
}

fun newCachedCappedScorecardSegmentStore(inner: CappedScorecardSegmentStore, ttl: Int, clock: Clock): CappedScorecardSegmentStore {
    return CachedCappedScorecardSegmentStore(
        inner = inner,
        cache = newTtlCache<CappedScorecardSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped scorecard segments. Empty fields match all. */
data class CappedScorecardSegmentQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedScorecardSegment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedScorecardSegment>): MutableList<CappedScorecardSegment> {
        val matched = filter(items) { candidate: CappedScorecardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedScorecardSegments(store: CappedScorecardSegmentStore, query: CappedScorecardSegmentQuery): MutableList<CappedScorecardSegment> {
    return query.apply(store.list())
}
