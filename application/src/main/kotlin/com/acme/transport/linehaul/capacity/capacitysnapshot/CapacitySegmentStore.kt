package com.acme.transport.linehaul.capacity.capacitysnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity segments by code. */
interface CapacitySegmentStore {
    fun find(code: String): CapacitySegment?
    fun save(item: CapacitySegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacitySegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacitySegmentStore(
    var items: MutableMap<String, CapacitySegment>,
    var order: MutableList<String>,
) : CapacitySegmentStore {
    override fun find(code: String): CapacitySegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacitySegment) {
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

    override fun list(): MutableList<CapacitySegment> {
        val out = mutableListOf<CapacitySegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacitySegmentStore(): CapacitySegmentStore {
    return MemoryCapacitySegmentStore(mutableMapOf<String, CapacitySegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacitySegmentStore(
    var inner: CapacitySegmentStore,
    var cache: TtlCache<CapacitySegment>,
    var hits: Int,
    var misses: Int,
) : CapacitySegmentStore {
    override fun find(code: String): CapacitySegment? {
        val hit = (cache.get(code) as CapacitySegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacitySegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacitySegment> = inner.list()
}

fun newCachedCapacitySegmentStore(inner: CapacitySegmentStore, ttl: Int, clock: Clock): CapacitySegmentStore {
    return CachedCapacitySegmentStore(
        inner = inner,
        cache = newTtlCache<CapacitySegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity segments. Empty fields match all. */
data class CapacitySegmentQuery(
    var lane: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacitySegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacitySegment>): MutableList<CapacitySegment> {
        val matched = filter(items) { candidate: CapacitySegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacitySegments(store: CapacitySegmentStore, query: CapacitySegmentQuery): MutableList<CapacitySegment> {
    return query.apply(store.list())
}
