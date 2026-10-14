package com.acme.transport.customs.clearance.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict clearance segments by code. */
interface StrictClearanceSegmentStore {
    fun find(code: String): StrictClearanceSegment?
    fun save(item: StrictClearanceSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictClearanceSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictClearanceSegmentStore(
    var items: MutableMap<String, StrictClearanceSegment>,
    var order: MutableList<String>,
) : StrictClearanceSegmentStore {
    override fun find(code: String): StrictClearanceSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictClearanceSegment) {
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

    override fun list(): MutableList<StrictClearanceSegment> {
        val out = mutableListOf<StrictClearanceSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictClearanceSegmentStore(): StrictClearanceSegmentStore {
    return MemoryStrictClearanceSegmentStore(
        mutableMapOf<String, StrictClearanceSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictClearanceSegmentStore(
    var inner: StrictClearanceSegmentStore,
    var cache: TtlCache<StrictClearanceSegment>,
    var hits: Int,
    var misses: Int,
) : StrictClearanceSegmentStore {
    override fun find(code: String): StrictClearanceSegment? {
        val hit = (cache.get(code) as StrictClearanceSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictClearanceSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictClearanceSegment> = inner.list()
}

fun newCachedStrictClearanceSegmentStore(inner: StrictClearanceSegmentStore, ttl: Int, clock: Clock): StrictClearanceSegmentStore {
    return CachedStrictClearanceSegmentStore(
        inner = inner,
        cache = newTtlCache<StrictClearanceSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict clearance segments. Empty fields match all. */
data class StrictClearanceSegmentQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictClearanceSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictClearanceSegment>): MutableList<StrictClearanceSegment> {
        val matched = filter(items) { candidate: StrictClearanceSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictClearanceSegments(store: StrictClearanceSegmentStore, query: StrictClearanceSegmentQuery): MutableList<StrictClearanceSegment> {
    return query.apply(store.list())
}
