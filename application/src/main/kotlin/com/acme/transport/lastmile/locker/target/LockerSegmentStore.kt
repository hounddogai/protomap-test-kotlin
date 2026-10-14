package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker segments by code. */
interface LockerSegmentStore {
    fun find(code: String): LockerSegment?
    fun save(item: LockerSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerSegmentStore(
    var items: MutableMap<String, LockerSegment>,
    var order: MutableList<String>,
) : LockerSegmentStore {
    override fun find(code: String): LockerSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerSegment) {
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

    override fun list(): MutableList<LockerSegment> {
        val out = mutableListOf<LockerSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerSegmentStore(): LockerSegmentStore {
    return MemoryLockerSegmentStore(mutableMapOf<String, LockerSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerSegmentStore(
    var inner: LockerSegmentStore,
    var cache: TtlCache<LockerSegment>,
    var hits: Int,
    var misses: Int,
) : LockerSegmentStore {
    override fun find(code: String): LockerSegment? {
        val hit = (cache.get(code) as LockerSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerSegment> = inner.list()
}

fun newCachedLockerSegmentStore(inner: LockerSegmentStore, ttl: Int, clock: Clock): LockerSegmentStore {
    return CachedLockerSegmentStore(
        inner = inner,
        cache = newTtlCache<LockerSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker segments. Empty fields match all. */
data class LockerSegmentQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerSegment>): MutableList<LockerSegment> {
        val matched = filter(items) { candidate: LockerSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerSegments(store: LockerSegmentStore, query: LockerSegmentQuery): MutableList<LockerSegment> {
    return query.apply(store.list())
}
