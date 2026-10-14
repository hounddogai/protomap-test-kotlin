package com.acme.transport.safety.coaching.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling coaching segments by code. */
interface RollingCoachingSegmentStore {
    fun find(code: String): RollingCoachingSegment?
    fun save(item: RollingCoachingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCoachingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCoachingSegmentStore(
    var items: MutableMap<String, RollingCoachingSegment>,
    var order: MutableList<String>,
) : RollingCoachingSegmentStore {
    override fun find(code: String): RollingCoachingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCoachingSegment) {
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

    override fun list(): MutableList<RollingCoachingSegment> {
        val out = mutableListOf<RollingCoachingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCoachingSegmentStore(): RollingCoachingSegmentStore {
    return MemoryRollingCoachingSegmentStore(
        mutableMapOf<String, RollingCoachingSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCoachingSegmentStore(
    var inner: RollingCoachingSegmentStore,
    var cache: TtlCache<RollingCoachingSegment>,
    var hits: Int,
    var misses: Int,
) : RollingCoachingSegmentStore {
    override fun find(code: String): RollingCoachingSegment? {
        val hit = (cache.get(code) as RollingCoachingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCoachingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCoachingSegment> = inner.list()
}

fun newCachedRollingCoachingSegmentStore(inner: RollingCoachingSegmentStore, ttl: Int, clock: Clock): RollingCoachingSegmentStore {
    return CachedRollingCoachingSegmentStore(
        inner = inner,
        cache = newTtlCache<RollingCoachingSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling coaching segments. Empty fields match all. */
data class RollingCoachingSegmentQuery(
    var locale: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCoachingSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCoachingSegment>): MutableList<RollingCoachingSegment> {
        val matched = filter(items) { candidate: RollingCoachingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCoachingSegments(store: RollingCoachingSegmentStore, query: RollingCoachingSegmentQuery): MutableList<RollingCoachingSegment> {
    return query.apply(store.list())
}
