package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident segments by code. */
interface IncidentSegmentStore {
    fun find(code: String): IncidentSegment?
    fun save(item: IncidentSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentSegmentStore(
    var items: MutableMap<String, IncidentSegment>,
    var order: MutableList<String>,
) : IncidentSegmentStore {
    override fun find(code: String): IncidentSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentSegment) {
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

    override fun list(): MutableList<IncidentSegment> {
        val out = mutableListOf<IncidentSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentSegmentStore(): IncidentSegmentStore {
    return MemoryIncidentSegmentStore(mutableMapOf<String, IncidentSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentSegmentStore(
    var inner: IncidentSegmentStore,
    var cache: TtlCache<IncidentSegment>,
    var hits: Int,
    var misses: Int,
) : IncidentSegmentStore {
    override fun find(code: String): IncidentSegment? {
        val hit = (cache.get(code) as IncidentSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentSegment> = inner.list()
}

fun newCachedIncidentSegmentStore(inner: IncidentSegmentStore, ttl: Int, clock: Clock): IncidentSegmentStore {
    return CachedIncidentSegmentStore(
        inner = inner,
        cache = newTtlCache<IncidentSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident segments. Empty fields match all. */
data class IncidentSegmentQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentSegment>): MutableList<IncidentSegment> {
        val matched = filter(items) { candidate: IncidentSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentSegments(store: IncidentSegmentStore, query: IncidentSegmentQuery): MutableList<IncidentSegment> {
    return query.apply(store.list())
}
