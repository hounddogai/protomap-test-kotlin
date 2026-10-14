package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global violation segments by code. */
interface GlobalViolationSegmentStore {
    fun find(code: String): GlobalViolationSegment?
    fun save(item: GlobalViolationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalViolationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalViolationSegmentStore(
    var items: MutableMap<String, GlobalViolationSegment>,
    var order: MutableList<String>,
) : GlobalViolationSegmentStore {
    override fun find(code: String): GlobalViolationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalViolationSegment) {
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

    override fun list(): MutableList<GlobalViolationSegment> {
        val out = mutableListOf<GlobalViolationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalViolationSegmentStore(): GlobalViolationSegmentStore {
    return MemoryGlobalViolationSegmentStore(
        mutableMapOf<String, GlobalViolationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalViolationSegmentStore(
    var inner: GlobalViolationSegmentStore,
    var cache: TtlCache<GlobalViolationSegment>,
    var hits: Int,
    var misses: Int,
) : GlobalViolationSegmentStore {
    override fun find(code: String): GlobalViolationSegment? {
        val hit = (cache.get(code) as GlobalViolationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalViolationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalViolationSegment> = inner.list()
}

fun newCachedGlobalViolationSegmentStore(inner: GlobalViolationSegmentStore, ttl: Int, clock: Clock): GlobalViolationSegmentStore {
    return CachedGlobalViolationSegmentStore(
        inner = inner,
        cache = newTtlCache<GlobalViolationSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global violation segments. Empty fields match all. */
data class GlobalViolationSegmentQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalViolationSegment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalViolationSegment>): MutableList<GlobalViolationSegment> {
        val matched = filter(items) { candidate: GlobalViolationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalViolationSegments(store: GlobalViolationSegmentStore, query: GlobalViolationSegmentQuery): MutableList<GlobalViolationSegment> {
    return query.apply(store.list())
}
