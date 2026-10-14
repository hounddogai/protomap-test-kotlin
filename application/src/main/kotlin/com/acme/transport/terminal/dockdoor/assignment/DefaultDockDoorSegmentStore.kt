package com.acme.transport.terminal.dockdoor.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default dock door segments by code. */
interface DefaultDockDoorSegmentStore {
    fun find(code: String): DefaultDockDoorSegment?
    fun save(item: DefaultDockDoorSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDockDoorSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDockDoorSegmentStore(
    var items: MutableMap<String, DefaultDockDoorSegment>,
    var order: MutableList<String>,
) : DefaultDockDoorSegmentStore {
    override fun find(code: String): DefaultDockDoorSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDockDoorSegment) {
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

    override fun list(): MutableList<DefaultDockDoorSegment> {
        val out = mutableListOf<DefaultDockDoorSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDockDoorSegmentStore(): DefaultDockDoorSegmentStore {
    return MemoryDefaultDockDoorSegmentStore(
        mutableMapOf<String, DefaultDockDoorSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDockDoorSegmentStore(
    var inner: DefaultDockDoorSegmentStore,
    var cache: TtlCache<DefaultDockDoorSegment>,
    var hits: Int,
    var misses: Int,
) : DefaultDockDoorSegmentStore {
    override fun find(code: String): DefaultDockDoorSegment? {
        val hit = (cache.get(code) as DefaultDockDoorSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDockDoorSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDockDoorSegment> = inner.list()
}

fun newCachedDefaultDockDoorSegmentStore(inner: DefaultDockDoorSegmentStore, ttl: Int, clock: Clock): DefaultDockDoorSegmentStore {
    return CachedDefaultDockDoorSegmentStore(
        inner = inner,
        cache = newTtlCache<DefaultDockDoorSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dock door segments. Empty fields match all. */
data class DefaultDockDoorSegmentQuery(
    var channel: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDockDoorSegment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<DefaultDockDoorSegment>): MutableList<DefaultDockDoorSegment> {
        val matched = filter(items) { candidate: DefaultDockDoorSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDockDoorSegments(store: DefaultDockDoorSegmentStore, query: DefaultDockDoorSegmentQuery): MutableList<DefaultDockDoorSegment> {
    return query.apply(store.list())
}
