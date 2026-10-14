package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual trailer segments by code. */
interface ManualTrailerSegmentStore {
    fun find(code: String): ManualTrailerSegment?
    fun save(item: ManualTrailerSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTrailerSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTrailerSegmentStore(
    var items: MutableMap<String, ManualTrailerSegment>,
    var order: MutableList<String>,
) : ManualTrailerSegmentStore {
    override fun find(code: String): ManualTrailerSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTrailerSegment) {
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

    override fun list(): MutableList<ManualTrailerSegment> {
        val out = mutableListOf<ManualTrailerSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTrailerSegmentStore(): ManualTrailerSegmentStore {
    return MemoryManualTrailerSegmentStore(
        mutableMapOf<String, ManualTrailerSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTrailerSegmentStore(
    var inner: ManualTrailerSegmentStore,
    var cache: TtlCache<ManualTrailerSegment>,
    var hits: Int,
    var misses: Int,
) : ManualTrailerSegmentStore {
    override fun find(code: String): ManualTrailerSegment? {
        val hit = (cache.get(code) as ManualTrailerSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTrailerSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTrailerSegment> = inner.list()
}

fun newCachedManualTrailerSegmentStore(inner: ManualTrailerSegmentStore, ttl: Int, clock: Clock): ManualTrailerSegmentStore {
    return CachedManualTrailerSegmentStore(
        inner = inner,
        cache = newTtlCache<ManualTrailerSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual trailer segments. Empty fields match all. */
data class ManualTrailerSegmentQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTrailerSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ManualTrailerSegment>): MutableList<ManualTrailerSegment> {
        val matched = filter(items) { candidate: ManualTrailerSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTrailerSegments(store: ManualTrailerSegmentStore, query: ManualTrailerSegmentQuery): MutableList<ManualTrailerSegment> {
    return query.apply(store.list())
}
