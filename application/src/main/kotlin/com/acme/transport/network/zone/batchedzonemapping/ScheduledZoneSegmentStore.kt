package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled zone segments by code. */
interface ScheduledZoneSegmentStore {
    fun find(code: String): ScheduledZoneSegment?
    fun save(item: ScheduledZoneSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledZoneSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledZoneSegmentStore(
    var items: MutableMap<String, ScheduledZoneSegment>,
    var order: MutableList<String>,
) : ScheduledZoneSegmentStore {
    override fun find(code: String): ScheduledZoneSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledZoneSegment) {
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

    override fun list(): MutableList<ScheduledZoneSegment> {
        val out = mutableListOf<ScheduledZoneSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledZoneSegmentStore(): ScheduledZoneSegmentStore {
    return MemoryScheduledZoneSegmentStore(
        mutableMapOf<String, ScheduledZoneSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledZoneSegmentStore(
    var inner: ScheduledZoneSegmentStore,
    var cache: TtlCache<ScheduledZoneSegment>,
    var hits: Int,
    var misses: Int,
) : ScheduledZoneSegmentStore {
    override fun find(code: String): ScheduledZoneSegment? {
        val hit = (cache.get(code) as ScheduledZoneSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledZoneSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledZoneSegment> = inner.list()
}

fun newCachedScheduledZoneSegmentStore(inner: ScheduledZoneSegmentStore, ttl: Int, clock: Clock): ScheduledZoneSegmentStore {
    return CachedScheduledZoneSegmentStore(
        inner = inner,
        cache = newTtlCache<ScheduledZoneSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled zone segments. Empty fields match all. */
data class ScheduledZoneSegmentQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledZoneSegment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledZoneSegment>): MutableList<ScheduledZoneSegment> {
        val matched = filter(items) { candidate: ScheduledZoneSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledZoneSegments(store: ScheduledZoneSegmentStore, query: ScheduledZoneSegmentQuery): MutableList<ScheduledZoneSegment> {
    return query.apply(store.list())
}
