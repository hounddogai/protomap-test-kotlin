package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance segments by code. */
interface ClearanceSegmentStore {
    fun find(code: String): ClearanceSegment?
    fun save(item: ClearanceSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceSegmentStore(
    var items: MutableMap<String, ClearanceSegment>,
    var order: MutableList<String>,
) : ClearanceSegmentStore {
    override fun find(code: String): ClearanceSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceSegment) {
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

    override fun list(): MutableList<ClearanceSegment> {
        val out = mutableListOf<ClearanceSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceSegmentStore(): ClearanceSegmentStore {
    return MemoryClearanceSegmentStore(mutableMapOf<String, ClearanceSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceSegmentStore(
    var inner: ClearanceSegmentStore,
    var cache: TtlCache<ClearanceSegment>,
    var hits: Int,
    var misses: Int,
) : ClearanceSegmentStore {
    override fun find(code: String): ClearanceSegment? {
        val hit = (cache.get(code) as ClearanceSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceSegment> = inner.list()
}

fun newCachedClearanceSegmentStore(inner: ClearanceSegmentStore, ttl: Int, clock: Clock): ClearanceSegmentStore {
    return CachedClearanceSegmentStore(
        inner = inner,
        cache = newTtlCache<ClearanceSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance segments. Empty fields match all. */
data class ClearanceSegmentQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceSegment>): MutableList<ClearanceSegment> {
        val matched = filter(items) { candidate: ClearanceSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceSegments(store: ClearanceSegmentStore, query: ClearanceSegmentQuery): MutableList<ClearanceSegment> {
    return query.apply(store.list())
}
