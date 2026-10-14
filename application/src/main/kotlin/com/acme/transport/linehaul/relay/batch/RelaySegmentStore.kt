package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay segments by code. */
interface RelaySegmentStore {
    fun find(code: String): RelaySegment?
    fun save(item: RelaySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelaySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelaySegmentStore(
    var items: MutableMap<String, RelaySegment>,
    var order: MutableList<String>,
) : RelaySegmentStore {
    override fun find(code: String): RelaySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelaySegment) {
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

    override fun list(): MutableList<RelaySegment> {
        val out = mutableListOf<RelaySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelaySegmentStore(): RelaySegmentStore {
    return MemoryRelaySegmentStore(mutableMapOf<String, RelaySegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelaySegmentStore(
    var inner: RelaySegmentStore,
    var cache: TtlCache<RelaySegment>,
    var hits: Int,
    var misses: Int,
) : RelaySegmentStore {
    override fun find(code: String): RelaySegment? {
        val hit = (cache.get(code) as RelaySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelaySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelaySegment> = inner.list()
}

fun newCachedRelaySegmentStore(inner: RelaySegmentStore, ttl: Int, clock: Clock): RelaySegmentStore {
    return CachedRelaySegmentStore(
        inner = inner,
        cache = newTtlCache<RelaySegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay segments. Empty fields match all. */
data class RelaySegmentQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelaySegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelaySegment>): MutableList<RelaySegment> {
        val matched = filter(items) { candidate: RelaySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelaySegments(store: RelaySegmentStore, query: RelaySegmentQuery): MutableList<RelaySegment> {
    return query.apply(store.list())
}
