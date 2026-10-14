package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback inspection segments by code. */
interface FallbackInspectionSegmentStore {
    fun find(code: String): FallbackInspectionSegment?
    fun save(item: FallbackInspectionSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackInspectionSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackInspectionSegmentStore(
    var items: MutableMap<String, FallbackInspectionSegment>,
    var order: MutableList<String>,
) : FallbackInspectionSegmentStore {
    override fun find(code: String): FallbackInspectionSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackInspectionSegment) {
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

    override fun list(): MutableList<FallbackInspectionSegment> {
        val out = mutableListOf<FallbackInspectionSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackInspectionSegmentStore(): FallbackInspectionSegmentStore {
    return MemoryFallbackInspectionSegmentStore(
        mutableMapOf<String, FallbackInspectionSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackInspectionSegmentStore(
    var inner: FallbackInspectionSegmentStore,
    var cache: TtlCache<FallbackInspectionSegment>,
    var hits: Int,
    var misses: Int,
) : FallbackInspectionSegmentStore {
    override fun find(code: String): FallbackInspectionSegment? {
        val hit = (cache.get(code) as FallbackInspectionSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackInspectionSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackInspectionSegment> = inner.list()
}

fun newCachedFallbackInspectionSegmentStore(inner: FallbackInspectionSegmentStore, ttl: Int, clock: Clock): FallbackInspectionSegmentStore {
    return CachedFallbackInspectionSegmentStore(
        inner = inner,
        cache = newTtlCache<FallbackInspectionSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback inspection segments. Empty fields match all. */
data class FallbackInspectionSegmentQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackInspectionSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackInspectionSegment>): MutableList<FallbackInspectionSegment> {
        val matched = filter(items) { candidate: FallbackInspectionSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackInspectionSegments(store: FallbackInspectionSegmentStore, query: FallbackInspectionSegmentQuery): MutableList<FallbackInspectionSegment> {
    return query.apply(store.list())
}
