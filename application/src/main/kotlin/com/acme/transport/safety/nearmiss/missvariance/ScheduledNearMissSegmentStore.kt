package com.acme.transport.safety.nearmiss.missvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled near miss segments by code. */
interface ScheduledNearMissSegmentStore {
    fun find(code: String): ScheduledNearMissSegment?
    fun save(item: ScheduledNearMissSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNearMissSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNearMissSegmentStore(
    var items: MutableMap<String, ScheduledNearMissSegment>,
    var order: MutableList<String>,
) : ScheduledNearMissSegmentStore {
    override fun find(code: String): ScheduledNearMissSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNearMissSegment) {
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

    override fun list(): MutableList<ScheduledNearMissSegment> {
        val out = mutableListOf<ScheduledNearMissSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNearMissSegmentStore(): ScheduledNearMissSegmentStore {
    return MemoryScheduledNearMissSegmentStore(
        mutableMapOf<String, ScheduledNearMissSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNearMissSegmentStore(
    var inner: ScheduledNearMissSegmentStore,
    var cache: TtlCache<ScheduledNearMissSegment>,
    var hits: Int,
    var misses: Int,
) : ScheduledNearMissSegmentStore {
    override fun find(code: String): ScheduledNearMissSegment? {
        val hit = (cache.get(code) as ScheduledNearMissSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNearMissSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNearMissSegment> = inner.list()
}

fun newCachedScheduledNearMissSegmentStore(inner: ScheduledNearMissSegmentStore, ttl: Int, clock: Clock): ScheduledNearMissSegmentStore {
    return CachedScheduledNearMissSegmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledNearMissSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled near miss segments. Empty fields match all. */
data class ScheduledNearMissSegmentQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNearMissSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledNearMissSegment>): MutableList<ScheduledNearMissSegment> {
        val matched = filter(items) { candidate: ScheduledNearMissSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNearMissSegments(store: ScheduledNearMissSegmentStore, query: ScheduledNearMissSegmentQuery): MutableList<ScheduledNearMissSegment> {
    return query.apply(store.list())
}
