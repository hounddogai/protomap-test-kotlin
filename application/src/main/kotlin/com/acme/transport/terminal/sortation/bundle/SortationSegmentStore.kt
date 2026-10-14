package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation segments by code. */
interface SortationSegmentStore {
    fun find(code: String): SortationSegment?
    fun save(item: SortationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationSegmentStore(
    var items: MutableMap<String, SortationSegment>,
    var order: MutableList<String>,
) : SortationSegmentStore {
    override fun find(code: String): SortationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationSegment) {
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

    override fun list(): MutableList<SortationSegment> {
        val out = mutableListOf<SortationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationSegmentStore(): SortationSegmentStore {
    return MemorySortationSegmentStore(mutableMapOf<String, SortationSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationSegmentStore(
    var inner: SortationSegmentStore,
    var cache: TtlCache<SortationSegment>,
    var hits: Int,
    var misses: Int,
) : SortationSegmentStore {
    override fun find(code: String): SortationSegment? {
        val hit = (cache.get(code) as SortationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationSegment> = inner.list()
}

fun newCachedSortationSegmentStore(inner: SortationSegmentStore, ttl: Int, clock: Clock): SortationSegmentStore {
    return CachedSortationSegmentStore(
        inner = inner,
        cache = newTtlCache<SortationSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation segments. Empty fields match all. */
data class SortationSegmentQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<SortationSegment>): MutableList<SortationSegment> {
        val matched = filter(items) { candidate: SortationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationSegments(store: SortationSegmentStore, query: SortationSegmentQuery): MutableList<SortationSegment> {
    return query.apply(store.list())
}
