package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient inspection segments by code. */
interface LenientInspectionSegmentStore {
    fun find(code: String): LenientInspectionSegment?
    fun save(item: LenientInspectionSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientInspectionSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientInspectionSegmentStore(
    var items: MutableMap<String, LenientInspectionSegment>,
    var order: MutableList<String>,
) : LenientInspectionSegmentStore {
    override fun find(code: String): LenientInspectionSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientInspectionSegment) {
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

    override fun list(): MutableList<LenientInspectionSegment> {
        val out = mutableListOf<LenientInspectionSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientInspectionSegmentStore(): LenientInspectionSegmentStore {
    return MemoryLenientInspectionSegmentStore(
        mutableMapOf<String, LenientInspectionSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientInspectionSegmentStore(
    var inner: LenientInspectionSegmentStore,
    var cache: TtlCache<LenientInspectionSegment>,
    var hits: Int,
    var misses: Int,
) : LenientInspectionSegmentStore {
    override fun find(code: String): LenientInspectionSegment? {
        val hit = (cache.get(code) as LenientInspectionSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientInspectionSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientInspectionSegment> = inner.list()
}

fun newCachedLenientInspectionSegmentStore(inner: LenientInspectionSegmentStore, ttl: Int, clock: Clock): LenientInspectionSegmentStore {
    return CachedLenientInspectionSegmentStore(
        inner = inner,
        cache = newTtlCache<LenientInspectionSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient inspection segments. Empty fields match all. */
data class LenientInspectionSegmentQuery(
    var locale: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientInspectionSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientInspectionSegment>): MutableList<LenientInspectionSegment> {
        val matched = filter(items) { candidate: LenientInspectionSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientInspectionSegments(store: LenientInspectionSegmentStore, query: LenientInspectionSegmentQuery): MutableList<LenientInspectionSegment> {
    return query.apply(store.list())
}
