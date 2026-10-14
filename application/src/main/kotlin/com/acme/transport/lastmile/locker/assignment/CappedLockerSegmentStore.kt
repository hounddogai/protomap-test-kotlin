package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped locker segments by code. */
interface CappedLockerSegmentStore {
    fun find(code: String): CappedLockerSegment?
    fun save(item: CappedLockerSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedLockerSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedLockerSegmentStore(
    var items: MutableMap<String, CappedLockerSegment>,
    var order: MutableList<String>,
) : CappedLockerSegmentStore {
    override fun find(code: String): CappedLockerSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedLockerSegment) {
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

    override fun list(): MutableList<CappedLockerSegment> {
        val out = mutableListOf<CappedLockerSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedLockerSegmentStore(): CappedLockerSegmentStore {
    return MemoryCappedLockerSegmentStore(mutableMapOf<String, CappedLockerSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedLockerSegmentStore(
    var inner: CappedLockerSegmentStore,
    var cache: TtlCache<CappedLockerSegment>,
    var hits: Int,
    var misses: Int,
) : CappedLockerSegmentStore {
    override fun find(code: String): CappedLockerSegment? {
        val hit = (cache.get(code) as CappedLockerSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedLockerSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedLockerSegment> = inner.list()
}

fun newCachedCappedLockerSegmentStore(inner: CappedLockerSegmentStore, ttl: Int, clock: Clock): CappedLockerSegmentStore {
    return CachedCappedLockerSegmentStore(
        inner = inner,
        cache = newTtlCache<CappedLockerSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped locker segments. Empty fields match all. */
data class CappedLockerSegmentQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedLockerSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedLockerSegment>): MutableList<CappedLockerSegment> {
        val matched = filter(items) { candidate: CappedLockerSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedLockerSegments(store: CappedLockerSegmentStore, query: CappedLockerSegmentQuery): MutableList<CappedLockerSegment> {
    return query.apply(store.list())
}
