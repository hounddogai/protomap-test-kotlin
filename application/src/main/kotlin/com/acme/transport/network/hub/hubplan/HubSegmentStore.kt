package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub segments by code. */
interface HubSegmentStore {
    fun find(code: String): HubSegment?
    fun save(item: HubSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubSegmentStore(
    var items: MutableMap<String, HubSegment>,
    var order: MutableList<String>,
) : HubSegmentStore {
    override fun find(code: String): HubSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubSegment) {
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

    override fun list(): MutableList<HubSegment> {
        val out = mutableListOf<HubSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubSegmentStore(): HubSegmentStore {
    return MemoryHubSegmentStore(mutableMapOf<String, HubSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubSegmentStore(
    var inner: HubSegmentStore,
    var cache: TtlCache<HubSegment>,
    var hits: Int,
    var misses: Int,
) : HubSegmentStore {
    override fun find(code: String): HubSegment? {
        val hit = (cache.get(code) as HubSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubSegment> = inner.list()
}

fun newCachedHubSegmentStore(inner: HubSegmentStore, ttl: Int, clock: Clock): HubSegmentStore {
    return CachedHubSegmentStore(
        inner = inner,
        cache = newTtlCache<HubSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub segments. Empty fields match all. */
data class HubSegmentQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<HubSegment>): MutableList<HubSegment> {
        val matched = filter(items) { candidate: HubSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubSegments(store: HubSegmentStore, query: HubSegmentQuery): MutableList<HubSegment> {
    return query.apply(store.list())
}
