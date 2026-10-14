package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary transit time segments by code. */
interface PrimaryTransitTimeSegmentStore {
    fun find(code: String): PrimaryTransitTimeSegment?
    fun save(item: PrimaryTransitTimeSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTransitTimeSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTransitTimeSegmentStore(
    var items: MutableMap<String, PrimaryTransitTimeSegment>,
    var order: MutableList<String>,
) : PrimaryTransitTimeSegmentStore {
    override fun find(code: String): PrimaryTransitTimeSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTransitTimeSegment) {
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

    override fun list(): MutableList<PrimaryTransitTimeSegment> {
        val out = mutableListOf<PrimaryTransitTimeSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTransitTimeSegmentStore(): PrimaryTransitTimeSegmentStore {
    return MemoryPrimaryTransitTimeSegmentStore(
        mutableMapOf<String, PrimaryTransitTimeSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTransitTimeSegmentStore(
    var inner: PrimaryTransitTimeSegmentStore,
    var cache: TtlCache<PrimaryTransitTimeSegment>,
    var hits: Int,
    var misses: Int,
) : PrimaryTransitTimeSegmentStore {
    override fun find(code: String): PrimaryTransitTimeSegment? {
        val hit = (cache.get(code) as PrimaryTransitTimeSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTransitTimeSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTransitTimeSegment> = inner.list()
}

fun newCachedPrimaryTransitTimeSegmentStore(inner: PrimaryTransitTimeSegmentStore, ttl: Int, clock: Clock): PrimaryTransitTimeSegmentStore {
    return CachedPrimaryTransitTimeSegmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryTransitTimeSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary transit time segments. Empty fields match all. */
data class PrimaryTransitTimeSegmentQuery(
    var source: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTransitTimeSegment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTransitTimeSegment>): MutableList<PrimaryTransitTimeSegment> {
        val matched = filter(items) { candidate: PrimaryTransitTimeSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTransitTimeSegments(store: PrimaryTransitTimeSegmentStore, query: PrimaryTransitTimeSegmentQuery): MutableList<PrimaryTransitTimeSegment> {
    return query.apply(store.list())
}
