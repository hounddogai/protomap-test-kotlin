package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global coaching segments by code. */
interface GlobalCoachingSegmentStore {
    fun find(code: String): GlobalCoachingSegment?
    fun save(item: GlobalCoachingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCoachingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCoachingSegmentStore(
    var items: MutableMap<String, GlobalCoachingSegment>,
    var order: MutableList<String>,
) : GlobalCoachingSegmentStore {
    override fun find(code: String): GlobalCoachingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCoachingSegment) {
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

    override fun list(): MutableList<GlobalCoachingSegment> {
        val out = mutableListOf<GlobalCoachingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCoachingSegmentStore(): GlobalCoachingSegmentStore {
    return MemoryGlobalCoachingSegmentStore(
        mutableMapOf<String, GlobalCoachingSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCoachingSegmentStore(
    var inner: GlobalCoachingSegmentStore,
    var cache: TtlCache<GlobalCoachingSegment>,
    var hits: Int,
    var misses: Int,
) : GlobalCoachingSegmentStore {
    override fun find(code: String): GlobalCoachingSegment? {
        val hit = (cache.get(code) as GlobalCoachingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCoachingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCoachingSegment> = inner.list()
}

fun newCachedGlobalCoachingSegmentStore(inner: GlobalCoachingSegmentStore, ttl: Int, clock: Clock): GlobalCoachingSegmentStore {
    return CachedGlobalCoachingSegmentStore(
        inner = inner,
        cache = newTtlCache<GlobalCoachingSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global coaching segments. Empty fields match all. */
data class GlobalCoachingSegmentQuery(
    var lane: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCoachingSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCoachingSegment>): MutableList<GlobalCoachingSegment> {
        val matched = filter(items) { candidate: GlobalCoachingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCoachingSegments(store: GlobalCoachingSegmentStore, query: GlobalCoachingSegmentQuery): MutableList<GlobalCoachingSegment> {
    return query.apply(store.list())
}
