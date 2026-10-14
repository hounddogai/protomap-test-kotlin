package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary incident segments by code. */
interface PrimaryIncidentSegmentStore {
    fun find(code: String): PrimaryIncidentSegment?
    fun save(item: PrimaryIncidentSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryIncidentSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryIncidentSegmentStore(
    var items: MutableMap<String, PrimaryIncidentSegment>,
    var order: MutableList<String>,
) : PrimaryIncidentSegmentStore {
    override fun find(code: String): PrimaryIncidentSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryIncidentSegment) {
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

    override fun list(): MutableList<PrimaryIncidentSegment> {
        val out = mutableListOf<PrimaryIncidentSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryIncidentSegmentStore(): PrimaryIncidentSegmentStore {
    return MemoryPrimaryIncidentSegmentStore(
        mutableMapOf<String, PrimaryIncidentSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryIncidentSegmentStore(
    var inner: PrimaryIncidentSegmentStore,
    var cache: TtlCache<PrimaryIncidentSegment>,
    var hits: Int,
    var misses: Int,
) : PrimaryIncidentSegmentStore {
    override fun find(code: String): PrimaryIncidentSegment? {
        val hit = (cache.get(code) as PrimaryIncidentSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryIncidentSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryIncidentSegment> = inner.list()
}

fun newCachedPrimaryIncidentSegmentStore(inner: PrimaryIncidentSegmentStore, ttl: Int, clock: Clock): PrimaryIncidentSegmentStore {
    return CachedPrimaryIncidentSegmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryIncidentSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary incident segments. Empty fields match all. */
data class PrimaryIncidentSegmentQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryIncidentSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryIncidentSegment>): MutableList<PrimaryIncidentSegment> {
        val matched = filter(items) { candidate: PrimaryIncidentSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryIncidentSegments(store: PrimaryIncidentSegmentStore, query: PrimaryIncidentSegmentQuery): MutableList<PrimaryIncidentSegment> {
    return query.apply(store.list())
}
