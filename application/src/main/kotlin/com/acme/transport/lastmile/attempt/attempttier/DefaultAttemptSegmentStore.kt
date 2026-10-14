package com.acme.transport.lastmile.attempt.attempttier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default attempt segments by code. */
interface DefaultAttemptSegmentStore {
    fun find(code: String): DefaultAttemptSegment?
    fun save(item: DefaultAttemptSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultAttemptSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultAttemptSegmentStore(
    var items: MutableMap<String, DefaultAttemptSegment>,
    var order: MutableList<String>,
) : DefaultAttemptSegmentStore {
    override fun find(code: String): DefaultAttemptSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultAttemptSegment) {
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

    override fun list(): MutableList<DefaultAttemptSegment> {
        val out = mutableListOf<DefaultAttemptSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultAttemptSegmentStore(): DefaultAttemptSegmentStore {
    return MemoryDefaultAttemptSegmentStore(
        mutableMapOf<String, DefaultAttemptSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultAttemptSegmentStore(
    var inner: DefaultAttemptSegmentStore,
    var cache: TtlCache<DefaultAttemptSegment>,
    var hits: Int,
    var misses: Int,
) : DefaultAttemptSegmentStore {
    override fun find(code: String): DefaultAttemptSegment? {
        val hit = (cache.get(code) as DefaultAttemptSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultAttemptSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultAttemptSegment> = inner.list()
}

fun newCachedDefaultAttemptSegmentStore(inner: DefaultAttemptSegmentStore, ttl: Int, clock: Clock): DefaultAttemptSegmentStore {
    return CachedDefaultAttemptSegmentStore(
        inner = inner,
        cache = newTtlCache<DefaultAttemptSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default attempt segments. Empty fields match all. */
data class DefaultAttemptSegmentQuery(
    var region: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultAttemptSegment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultAttemptSegment>): MutableList<DefaultAttemptSegment> {
        val matched = filter(items) { candidate: DefaultAttemptSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultAttemptSegments(store: DefaultAttemptSegmentStore, query: DefaultAttemptSegmentQuery): MutableList<DefaultAttemptSegment> {
    return query.apply(store.list())
}
