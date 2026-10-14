package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary near miss segments by code. */
interface PrimaryNearMissSegmentStore {
    fun find(code: String): PrimaryNearMissSegment?
    fun save(item: PrimaryNearMissSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryNearMissSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryNearMissSegmentStore(
    var items: MutableMap<String, PrimaryNearMissSegment>,
    var order: MutableList<String>,
) : PrimaryNearMissSegmentStore {
    override fun find(code: String): PrimaryNearMissSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryNearMissSegment) {
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

    override fun list(): MutableList<PrimaryNearMissSegment> {
        val out = mutableListOf<PrimaryNearMissSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryNearMissSegmentStore(): PrimaryNearMissSegmentStore {
    return MemoryPrimaryNearMissSegmentStore(
        mutableMapOf<String, PrimaryNearMissSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryNearMissSegmentStore(
    var inner: PrimaryNearMissSegmentStore,
    var cache: TtlCache<PrimaryNearMissSegment>,
    var hits: Int,
    var misses: Int,
) : PrimaryNearMissSegmentStore {
    override fun find(code: String): PrimaryNearMissSegment? {
        val hit = (cache.get(code) as PrimaryNearMissSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryNearMissSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryNearMissSegment> = inner.list()
}

fun newCachedPrimaryNearMissSegmentStore(inner: PrimaryNearMissSegmentStore, ttl: Int, clock: Clock): PrimaryNearMissSegmentStore {
    return CachedPrimaryNearMissSegmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryNearMissSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary near miss segments. Empty fields match all. */
data class PrimaryNearMissSegmentQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryNearMissSegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryNearMissSegment>): MutableList<PrimaryNearMissSegment> {
        val matched = filter(items) { candidate: PrimaryNearMissSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryNearMissSegments(store: PrimaryNearMissSegmentStore, query: PrimaryNearMissSegmentQuery): MutableList<PrimaryNearMissSegment> {
    return query.apply(store.list())
}
