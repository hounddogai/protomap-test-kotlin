package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching segments by code. */
interface CoachingSegmentStore {
    fun find(code: String): CoachingSegment?
    fun save(item: CoachingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingSegmentStore(
    var items: MutableMap<String, CoachingSegment>,
    var order: MutableList<String>,
) : CoachingSegmentStore {
    override fun find(code: String): CoachingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingSegment) {
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

    override fun list(): MutableList<CoachingSegment> {
        val out = mutableListOf<CoachingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingSegmentStore(): CoachingSegmentStore {
    return MemoryCoachingSegmentStore(mutableMapOf<String, CoachingSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingSegmentStore(
    var inner: CoachingSegmentStore,
    var cache: TtlCache<CoachingSegment>,
    var hits: Int,
    var misses: Int,
) : CoachingSegmentStore {
    override fun find(code: String): CoachingSegment? {
        val hit = (cache.get(code) as CoachingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingSegment> = inner.list()
}

fun newCachedCoachingSegmentStore(inner: CoachingSegmentStore, ttl: Int, clock: Clock): CoachingSegmentStore {
    return CachedCoachingSegmentStore(
        inner = inner,
        cache = newTtlCache<CoachingSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching segments. Empty fields match all. */
data class CoachingSegmentQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingSegment>): MutableList<CoachingSegment> {
        val matched = filter(items) { candidate: CoachingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingSegments(store: CoachingSegmentStore, query: CoachingSegmentQuery): MutableList<CoachingSegment> {
    return query.apply(store.list())
}
