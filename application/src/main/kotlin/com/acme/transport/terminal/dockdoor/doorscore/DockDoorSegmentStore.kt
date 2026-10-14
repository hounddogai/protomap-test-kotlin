package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door segments by code. */
interface DockDoorSegmentStore {
    fun find(code: String): DockDoorSegment?
    fun save(item: DockDoorSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorSegmentStore(
    var items: MutableMap<String, DockDoorSegment>,
    var order: MutableList<String>,
) : DockDoorSegmentStore {
    override fun find(code: String): DockDoorSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorSegment) {
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

    override fun list(): MutableList<DockDoorSegment> {
        val out = mutableListOf<DockDoorSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorSegmentStore(): DockDoorSegmentStore {
    return MemoryDockDoorSegmentStore(mutableMapOf<String, DockDoorSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorSegmentStore(
    var inner: DockDoorSegmentStore,
    var cache: TtlCache<DockDoorSegment>,
    var hits: Int,
    var misses: Int,
) : DockDoorSegmentStore {
    override fun find(code: String): DockDoorSegment? {
        val hit = (cache.get(code) as DockDoorSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorSegment> = inner.list()
}

fun newCachedDockDoorSegmentStore(inner: DockDoorSegmentStore, ttl: Int, clock: Clock): DockDoorSegmentStore {
    return CachedDockDoorSegmentStore(
        inner = inner,
        cache = newTtlCache<DockDoorSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door segments. Empty fields match all. */
data class DockDoorSegmentQuery(
    var locale: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorSegment>): MutableList<DockDoorSegment> {
        val matched = filter(items) { candidate: DockDoorSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorSegments(store: DockDoorSegmentStore, query: DockDoorSegmentQuery): MutableList<DockDoorSegment> {
    return query.apply(store.list())
}
