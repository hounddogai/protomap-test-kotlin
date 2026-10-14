package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual incident segments by code. */
interface ManualIncidentSegmentStore {
    fun find(code: String): ManualIncidentSegment?
    fun save(item: ManualIncidentSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualIncidentSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualIncidentSegmentStore(
    var items: MutableMap<String, ManualIncidentSegment>,
    var order: MutableList<String>,
) : ManualIncidentSegmentStore {
    override fun find(code: String): ManualIncidentSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualIncidentSegment) {
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

    override fun list(): MutableList<ManualIncidentSegment> {
        val out = mutableListOf<ManualIncidentSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualIncidentSegmentStore(): ManualIncidentSegmentStore {
    return MemoryManualIncidentSegmentStore(
        mutableMapOf<String, ManualIncidentSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualIncidentSegmentStore(
    var inner: ManualIncidentSegmentStore,
    var cache: TtlCache<ManualIncidentSegment>,
    var hits: Int,
    var misses: Int,
) : ManualIncidentSegmentStore {
    override fun find(code: String): ManualIncidentSegment? {
        val hit = (cache.get(code) as ManualIncidentSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualIncidentSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualIncidentSegment> = inner.list()
}

fun newCachedManualIncidentSegmentStore(inner: ManualIncidentSegmentStore, ttl: Int, clock: Clock): ManualIncidentSegmentStore {
    return CachedManualIncidentSegmentStore(
        inner = inner,
        cache = newTtlCache<ManualIncidentSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual incident segments. Empty fields match all. */
data class ManualIncidentSegmentQuery(
    var segment: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualIncidentSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualIncidentSegment>): MutableList<ManualIncidentSegment> {
        val matched = filter(items) { candidate: ManualIncidentSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualIncidentSegments(store: ManualIncidentSegmentStore, query: ManualIncidentSegmentQuery): MutableList<ManualIncidentSegment> {
    return query.apply(store.list())
}
