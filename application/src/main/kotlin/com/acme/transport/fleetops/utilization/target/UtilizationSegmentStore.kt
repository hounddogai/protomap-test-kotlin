package com.acme.transport.fleetops.utilization.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization segments by code. */
interface UtilizationSegmentStore {
    fun find(code: String): UtilizationSegment?
    fun save(item: UtilizationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationSegmentStore(
    var items: MutableMap<String, UtilizationSegment>,
    var order: MutableList<String>,
) : UtilizationSegmentStore {
    override fun find(code: String): UtilizationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationSegment) {
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

    override fun list(): MutableList<UtilizationSegment> {
        val out = mutableListOf<UtilizationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationSegmentStore(): UtilizationSegmentStore {
    return MemoryUtilizationSegmentStore(mutableMapOf<String, UtilizationSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationSegmentStore(
    var inner: UtilizationSegmentStore,
    var cache: TtlCache<UtilizationSegment>,
    var hits: Int,
    var misses: Int,
) : UtilizationSegmentStore {
    override fun find(code: String): UtilizationSegment? {
        val hit = (cache.get(code) as UtilizationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationSegment> = inner.list()
}

fun newCachedUtilizationSegmentStore(inner: UtilizationSegmentStore, ttl: Int, clock: Clock): UtilizationSegmentStore {
    return CachedUtilizationSegmentStore(
        inner = inner,
        cache = newTtlCache<UtilizationSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization segments. Empty fields match all. */
data class UtilizationSegmentQuery(
    var locale: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationSegment>): MutableList<UtilizationSegment> {
        val matched = filter(items) { candidate: UtilizationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationSegments(store: UtilizationSegmentStore, query: UtilizationSegmentQuery): MutableList<UtilizationSegment> {
    return query.apply(store.list())
}
