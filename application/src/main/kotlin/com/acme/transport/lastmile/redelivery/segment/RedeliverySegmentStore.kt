package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery segments by code. */
interface RedeliverySegmentStore {
    fun find(code: String): RedeliverySegment?
    fun save(item: RedeliverySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliverySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliverySegmentStore(
    var items: MutableMap<String, RedeliverySegment>,
    var order: MutableList<String>,
) : RedeliverySegmentStore {
    override fun find(code: String): RedeliverySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliverySegment) {
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

    override fun list(): MutableList<RedeliverySegment> {
        val out = mutableListOf<RedeliverySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliverySegmentStore(): RedeliverySegmentStore {
    return MemoryRedeliverySegmentStore(mutableMapOf<String, RedeliverySegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliverySegmentStore(
    var inner: RedeliverySegmentStore,
    var cache: TtlCache<RedeliverySegment>,
    var hits: Int,
    var misses: Int,
) : RedeliverySegmentStore {
    override fun find(code: String): RedeliverySegment? {
        val hit = (cache.get(code) as RedeliverySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliverySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliverySegment> = inner.list()
}

fun newCachedRedeliverySegmentStore(inner: RedeliverySegmentStore, ttl: Int, clock: Clock): RedeliverySegmentStore {
    return CachedRedeliverySegmentStore(
        inner = inner,
        cache = newTtlCache<RedeliverySegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery segments. Empty fields match all. */
data class RedeliverySegmentQuery(
    var unit: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliverySegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliverySegment>): MutableList<RedeliverySegment> {
        val matched = filter(items) { candidate: RedeliverySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliverySegments(store: RedeliverySegmentStore, query: RedeliverySegmentQuery): MutableList<RedeliverySegment> {
    return query.apply(store.list())
}
