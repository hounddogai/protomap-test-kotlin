package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending sortation segments by code. */
interface PendingSortationSegmentStore {
    fun find(code: String): PendingSortationSegment?
    fun save(item: PendingSortationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingSortationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingSortationSegmentStore(
    var items: MutableMap<String, PendingSortationSegment>,
    var order: MutableList<String>,
) : PendingSortationSegmentStore {
    override fun find(code: String): PendingSortationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingSortationSegment) {
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

    override fun list(): MutableList<PendingSortationSegment> {
        val out = mutableListOf<PendingSortationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingSortationSegmentStore(): PendingSortationSegmentStore {
    return MemoryPendingSortationSegmentStore(
        mutableMapOf<String, PendingSortationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingSortationSegmentStore(
    var inner: PendingSortationSegmentStore,
    var cache: TtlCache<PendingSortationSegment>,
    var hits: Int,
    var misses: Int,
) : PendingSortationSegmentStore {
    override fun find(code: String): PendingSortationSegment? {
        val hit = (cache.get(code) as PendingSortationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingSortationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingSortationSegment> = inner.list()
}

fun newCachedPendingSortationSegmentStore(inner: PendingSortationSegmentStore, ttl: Int, clock: Clock): PendingSortationSegmentStore {
    return CachedPendingSortationSegmentStore(
        inner = inner,
        cache = newTtlCache<PendingSortationSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending sortation segments. Empty fields match all. */
data class PendingSortationSegmentQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingSortationSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingSortationSegment>): MutableList<PendingSortationSegment> {
        val matched = filter(items) { candidate: PendingSortationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingSortationSegments(store: PendingSortationSegmentStore, query: PendingSortationSegmentQuery): MutableList<PendingSortationSegment> {
    return query.apply(store.list())
}
