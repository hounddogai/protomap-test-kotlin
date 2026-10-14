package com.acme.transport.customs.inspection.metric

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection segments by code. */
interface InspectionSegmentStore {
    fun find(code: String): InspectionSegment?
    fun save(item: InspectionSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionSegmentStore(
    var items: MutableMap<String, InspectionSegment>,
    var order: MutableList<String>,
) : InspectionSegmentStore {
    override fun find(code: String): InspectionSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionSegment) {
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

    override fun list(): MutableList<InspectionSegment> {
        val out = mutableListOf<InspectionSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionSegmentStore(): InspectionSegmentStore {
    return MemoryInspectionSegmentStore(mutableMapOf<String, InspectionSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionSegmentStore(
    var inner: InspectionSegmentStore,
    var cache: TtlCache<InspectionSegment>,
    var hits: Int,
    var misses: Int,
) : InspectionSegmentStore {
    override fun find(code: String): InspectionSegment? {
        val hit = (cache.get(code) as InspectionSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionSegment> = inner.list()
}

fun newCachedInspectionSegmentStore(inner: InspectionSegmentStore, ttl: Int, clock: Clock): InspectionSegmentStore {
    return CachedInspectionSegmentStore(
        inner = inner,
        cache = newTtlCache<InspectionSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection segments. Empty fields match all. */
data class InspectionSegmentQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionSegment>): MutableList<InspectionSegment> {
        val matched = filter(items) { candidate: InspectionSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionSegments(store: InspectionSegmentStore, query: InspectionSegmentQuery): MutableList<InspectionSegment> {
    return query.apply(store.list())
}
