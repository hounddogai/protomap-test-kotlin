package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global service area segments by code. */
interface GlobalServiceAreaSegmentStore {
    fun find(code: String): GlobalServiceAreaSegment?
    fun save(item: GlobalServiceAreaSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalServiceAreaSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalServiceAreaSegmentStore(
    var items: MutableMap<String, GlobalServiceAreaSegment>,
    var order: MutableList<String>,
) : GlobalServiceAreaSegmentStore {
    override fun find(code: String): GlobalServiceAreaSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalServiceAreaSegment) {
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

    override fun list(): MutableList<GlobalServiceAreaSegment> {
        val out = mutableListOf<GlobalServiceAreaSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalServiceAreaSegmentStore(): GlobalServiceAreaSegmentStore {
    return MemoryGlobalServiceAreaSegmentStore(
        mutableMapOf<String, GlobalServiceAreaSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalServiceAreaSegmentStore(
    var inner: GlobalServiceAreaSegmentStore,
    var cache: TtlCache<GlobalServiceAreaSegment>,
    var hits: Int,
    var misses: Int,
) : GlobalServiceAreaSegmentStore {
    override fun find(code: String): GlobalServiceAreaSegment? {
        val hit = (cache.get(code) as GlobalServiceAreaSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalServiceAreaSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalServiceAreaSegment> = inner.list()
}

fun newCachedGlobalServiceAreaSegmentStore(inner: GlobalServiceAreaSegmentStore, ttl: Int, clock: Clock): GlobalServiceAreaSegmentStore {
    return CachedGlobalServiceAreaSegmentStore(
        inner = inner,
        cache = newTtlCache<GlobalServiceAreaSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global service area segments. Empty fields match all. */
data class GlobalServiceAreaSegmentQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalServiceAreaSegment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<GlobalServiceAreaSegment>): MutableList<GlobalServiceAreaSegment> {
        val matched = filter(items) { candidate: GlobalServiceAreaSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalServiceAreaSegments(store: GlobalServiceAreaSegmentStore, query: GlobalServiceAreaSegmentQuery): MutableList<GlobalServiceAreaSegment> {
    return query.apply(store.list())
}
