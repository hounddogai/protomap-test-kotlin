package com.acme.transport.emissions.idling.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict idling segments by code. */
interface StrictIdlingSegmentStore {
    fun find(code: String): StrictIdlingSegment?
    fun save(item: StrictIdlingSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIdlingSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIdlingSegmentStore(
    var items: MutableMap<String, StrictIdlingSegment>,
    var order: MutableList<String>,
) : StrictIdlingSegmentStore {
    override fun find(code: String): StrictIdlingSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIdlingSegment) {
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

    override fun list(): MutableList<StrictIdlingSegment> {
        val out = mutableListOf<StrictIdlingSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIdlingSegmentStore(): StrictIdlingSegmentStore {
    return MemoryStrictIdlingSegmentStore(mutableMapOf<String, StrictIdlingSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIdlingSegmentStore(
    var inner: StrictIdlingSegmentStore,
    var cache: TtlCache<StrictIdlingSegment>,
    var hits: Int,
    var misses: Int,
) : StrictIdlingSegmentStore {
    override fun find(code: String): StrictIdlingSegment? {
        val hit = (cache.get(code) as StrictIdlingSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIdlingSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIdlingSegment> = inner.list()
}

fun newCachedStrictIdlingSegmentStore(inner: StrictIdlingSegmentStore, ttl: Int, clock: Clock): StrictIdlingSegmentStore {
    return CachedStrictIdlingSegmentStore(
        inner = inner,
        cache = newTtlCache<StrictIdlingSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict idling segments. Empty fields match all. */
data class StrictIdlingSegmentQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIdlingSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictIdlingSegment>): MutableList<StrictIdlingSegment> {
        val matched = filter(items) { candidate: StrictIdlingSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIdlingSegments(store: StrictIdlingSegmentStore, query: StrictIdlingSegmentQuery): MutableList<StrictIdlingSegment> {
    return query.apply(store.list())
}
