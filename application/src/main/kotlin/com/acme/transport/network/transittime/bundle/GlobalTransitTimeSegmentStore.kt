package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global transit time segments by code. */
interface GlobalTransitTimeSegmentStore {
    fun find(code: String): GlobalTransitTimeSegment?
    fun save(item: GlobalTransitTimeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTransitTimeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTransitTimeSegmentStore(
    var items: MutableMap<String, GlobalTransitTimeSegment>,
    var order: MutableList<String>,
) : GlobalTransitTimeSegmentStore {
    override fun find(code: String): GlobalTransitTimeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTransitTimeSegment) {
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

    override fun list(): MutableList<GlobalTransitTimeSegment> {
        val out = mutableListOf<GlobalTransitTimeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTransitTimeSegmentStore(): GlobalTransitTimeSegmentStore {
    return MemoryGlobalTransitTimeSegmentStore(
        mutableMapOf<String, GlobalTransitTimeSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTransitTimeSegmentStore(
    var inner: GlobalTransitTimeSegmentStore,
    var cache: TtlCache<GlobalTransitTimeSegment>,
    var hits: Int,
    var misses: Int,
) : GlobalTransitTimeSegmentStore {
    override fun find(code: String): GlobalTransitTimeSegment? {
        val hit = (cache.get(code) as GlobalTransitTimeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTransitTimeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTransitTimeSegment> = inner.list()
}

fun newCachedGlobalTransitTimeSegmentStore(inner: GlobalTransitTimeSegmentStore, ttl: Int, clock: Clock): GlobalTransitTimeSegmentStore {
    return CachedGlobalTransitTimeSegmentStore(
        inner = inner,
        cache = newTtlCache<GlobalTransitTimeSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global transit time segments. Empty fields match all. */
data class GlobalTransitTimeSegmentQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTransitTimeSegment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<GlobalTransitTimeSegment>): MutableList<GlobalTransitTimeSegment> {
        val matched = filter(items) { candidate: GlobalTransitTimeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTransitTimeSegments(store: GlobalTransitTimeSegmentStore, query: GlobalTransitTimeSegmentQuery): MutableList<GlobalTransitTimeSegment> {
    return query.apply(store.list())
}
