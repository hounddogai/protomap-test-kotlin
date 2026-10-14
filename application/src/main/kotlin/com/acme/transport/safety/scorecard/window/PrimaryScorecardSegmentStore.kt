package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary scorecard segments by code. */
interface PrimaryScorecardSegmentStore {
    fun find(code: String): PrimaryScorecardSegment?
    fun save(item: PrimaryScorecardSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryScorecardSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryScorecardSegmentStore(
    var items: MutableMap<String, PrimaryScorecardSegment>,
    var order: MutableList<String>,
) : PrimaryScorecardSegmentStore {
    override fun find(code: String): PrimaryScorecardSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryScorecardSegment) {
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

    override fun list(): MutableList<PrimaryScorecardSegment> {
        val out = mutableListOf<PrimaryScorecardSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryScorecardSegmentStore(): PrimaryScorecardSegmentStore {
    return MemoryPrimaryScorecardSegmentStore(
        mutableMapOf<String, PrimaryScorecardSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryScorecardSegmentStore(
    var inner: PrimaryScorecardSegmentStore,
    var cache: TtlCache<PrimaryScorecardSegment>,
    var hits: Int,
    var misses: Int,
) : PrimaryScorecardSegmentStore {
    override fun find(code: String): PrimaryScorecardSegment? {
        val hit = (cache.get(code) as PrimaryScorecardSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryScorecardSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryScorecardSegment> = inner.list()
}

fun newCachedPrimaryScorecardSegmentStore(inner: PrimaryScorecardSegmentStore, ttl: Int, clock: Clock): PrimaryScorecardSegmentStore {
    return CachedPrimaryScorecardSegmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryScorecardSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary scorecard segments. Empty fields match all. */
data class PrimaryScorecardSegmentQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryScorecardSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryScorecardSegment>): MutableList<PrimaryScorecardSegment> {
        val matched = filter(items) { candidate: PrimaryScorecardSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryScorecardSegments(store: PrimaryScorecardSegmentStore, query: PrimaryScorecardSegmentQuery): MutableList<PrimaryScorecardSegment> {
    return query.apply(store.list())
}
