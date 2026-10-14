package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time segments by code. */
interface TransitTimeSegmentStore {
    fun find(code: String): TransitTimeSegment?
    fun save(item: TransitTimeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeSegmentStore(
    var items: MutableMap<String, TransitTimeSegment>,
    var order: MutableList<String>,
) : TransitTimeSegmentStore {
    override fun find(code: String): TransitTimeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeSegment) {
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

    override fun list(): MutableList<TransitTimeSegment> {
        val out = mutableListOf<TransitTimeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeSegmentStore(): TransitTimeSegmentStore {
    return MemoryTransitTimeSegmentStore(mutableMapOf<String, TransitTimeSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeSegmentStore(
    var inner: TransitTimeSegmentStore,
    var cache: TtlCache<TransitTimeSegment>,
    var hits: Int,
    var misses: Int,
) : TransitTimeSegmentStore {
    override fun find(code: String): TransitTimeSegment? {
        val hit = (cache.get(code) as TransitTimeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeSegment> = inner.list()
}

fun newCachedTransitTimeSegmentStore(inner: TransitTimeSegmentStore, ttl: Int, clock: Clock): TransitTimeSegmentStore {
    return CachedTransitTimeSegmentStore(
        inner = inner,
        cache = newTtlCache<TransitTimeSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time segments. Empty fields match all. */
data class TransitTimeSegmentQuery(
    var channel: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeSegment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeSegment>): MutableList<TransitTimeSegment> {
        val matched = filter(items) { candidate: TransitTimeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeSegments(store: TransitTimeSegmentStore, query: TransitTimeSegmentQuery): MutableList<TransitTimeSegment> {
    return query.apply(store.list())
}
