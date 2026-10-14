package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt segments by code. */
interface AttemptSegmentStore {
    fun find(code: String): AttemptSegment?
    fun save(item: AttemptSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptSegmentStore(
    var items: MutableMap<String, AttemptSegment>,
    var order: MutableList<String>,
) : AttemptSegmentStore {
    override fun find(code: String): AttemptSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptSegment) {
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

    override fun list(): MutableList<AttemptSegment> {
        val out = mutableListOf<AttemptSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptSegmentStore(): AttemptSegmentStore {
    return MemoryAttemptSegmentStore(mutableMapOf<String, AttemptSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptSegmentStore(
    var inner: AttemptSegmentStore,
    var cache: TtlCache<AttemptSegment>,
    var hits: Int,
    var misses: Int,
) : AttemptSegmentStore {
    override fun find(code: String): AttemptSegment? {
        val hit = (cache.get(code) as AttemptSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptSegment> = inner.list()
}

fun newCachedAttemptSegmentStore(inner: AttemptSegmentStore, ttl: Int, clock: Clock): AttemptSegmentStore {
    return CachedAttemptSegmentStore(
        inner = inner,
        cache = newTtlCache<AttemptSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt segments. Empty fields match all. */
data class AttemptSegmentQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AttemptSegment>): MutableList<AttemptSegment> {
        val matched = filter(items) { candidate: AttemptSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptSegments(store: AttemptSegmentStore, query: AttemptSegmentQuery): MutableList<AttemptSegment> {
    return query.apply(store.list())
}
