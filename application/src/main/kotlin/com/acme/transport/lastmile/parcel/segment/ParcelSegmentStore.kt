package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel segments by code. */
interface ParcelSegmentStore {
    fun find(code: String): ParcelSegment?
    fun save(item: ParcelSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelSegmentStore(
    var items: MutableMap<String, ParcelSegment>,
    var order: MutableList<String>,
) : ParcelSegmentStore {
    override fun find(code: String): ParcelSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelSegment) {
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

    override fun list(): MutableList<ParcelSegment> {
        val out = mutableListOf<ParcelSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelSegmentStore(): ParcelSegmentStore {
    return MemoryParcelSegmentStore(mutableMapOf<String, ParcelSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelSegmentStore(
    var inner: ParcelSegmentStore,
    var cache: TtlCache<ParcelSegment>,
    var hits: Int,
    var misses: Int,
) : ParcelSegmentStore {
    override fun find(code: String): ParcelSegment? {
        val hit = (cache.get(code) as ParcelSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelSegment> = inner.list()
}

fun newCachedParcelSegmentStore(inner: ParcelSegmentStore, ttl: Int, clock: Clock): ParcelSegmentStore {
    return CachedParcelSegmentStore(
        inner = inner,
        cache = newTtlCache<ParcelSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel segments. Empty fields match all. */
data class ParcelSegmentQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelSegment>): MutableList<ParcelSegment> {
        val matched = filter(items) { candidate: ParcelSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelSegments(store: ParcelSegmentStore, query: ParcelSegmentQuery): MutableList<ParcelSegment> {
    return query.apply(store.list())
}
