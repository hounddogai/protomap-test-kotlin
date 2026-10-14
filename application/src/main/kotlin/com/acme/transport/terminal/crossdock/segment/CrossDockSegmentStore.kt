package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock segments by code. */
interface CrossDockSegmentStore {
    fun find(code: String): CrossDockSegment?
    fun save(item: CrossDockSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockSegmentStore(
    var items: MutableMap<String, CrossDockSegment>,
    var order: MutableList<String>,
) : CrossDockSegmentStore {
    override fun find(code: String): CrossDockSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockSegment) {
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

    override fun list(): MutableList<CrossDockSegment> {
        val out = mutableListOf<CrossDockSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockSegmentStore(): CrossDockSegmentStore {
    return MemoryCrossDockSegmentStore(mutableMapOf<String, CrossDockSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockSegmentStore(
    var inner: CrossDockSegmentStore,
    var cache: TtlCache<CrossDockSegment>,
    var hits: Int,
    var misses: Int,
) : CrossDockSegmentStore {
    override fun find(code: String): CrossDockSegment? {
        val hit = (cache.get(code) as CrossDockSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockSegment> = inner.list()
}

fun newCachedCrossDockSegmentStore(inner: CrossDockSegmentStore, ttl: Int, clock: Clock): CrossDockSegmentStore {
    return CachedCrossDockSegmentStore(
        inner = inner,
        cache = newTtlCache<CrossDockSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock segments. Empty fields match all. */
data class CrossDockSegmentQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockSegment>): MutableList<CrossDockSegment> {
        val matched = filter(items) { candidate: CrossDockSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockSegments(store: CrossDockSegmentStore, query: CrossDockSegmentQuery): MutableList<CrossDockSegment> {
    return query.apply(store.list())
}
