package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling near miss segments by code. */
interface RollingNearMissSegmentStore {
    fun find(code: String): RollingNearMissSegment?
    fun save(item: RollingNearMissSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingNearMissSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingNearMissSegmentStore(
    var items: MutableMap<String, RollingNearMissSegment>,
    var order: MutableList<String>,
) : RollingNearMissSegmentStore {
    override fun find(code: String): RollingNearMissSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingNearMissSegment) {
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

    override fun list(): MutableList<RollingNearMissSegment> {
        val out = mutableListOf<RollingNearMissSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingNearMissSegmentStore(): RollingNearMissSegmentStore {
    return MemoryRollingNearMissSegmentStore(
        mutableMapOf<String, RollingNearMissSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingNearMissSegmentStore(
    var inner: RollingNearMissSegmentStore,
    var cache: TtlCache<RollingNearMissSegment>,
    var hits: Int,
    var misses: Int,
) : RollingNearMissSegmentStore {
    override fun find(code: String): RollingNearMissSegment? {
        val hit = (cache.get(code) as RollingNearMissSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingNearMissSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingNearMissSegment> = inner.list()
}

fun newCachedRollingNearMissSegmentStore(inner: RollingNearMissSegmentStore, ttl: Int, clock: Clock): RollingNearMissSegmentStore {
    return CachedRollingNearMissSegmentStore(
        inner = inner,
        cache = newTtlCache<RollingNearMissSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling near miss segments. Empty fields match all. */
data class RollingNearMissSegmentQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingNearMissSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingNearMissSegment>): MutableList<RollingNearMissSegment> {
        val matched = filter(items) { candidate: RollingNearMissSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingNearMissSegments(store: RollingNearMissSegmentStore, query: RollingNearMissSegmentQuery): MutableList<RollingNearMissSegment> {
    return query.apply(store.list())
}
