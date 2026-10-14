package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict roster segments by code. */
interface StrictRosterSegmentStore {
    fun find(code: String): StrictRosterSegment?
    fun save(item: StrictRosterSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRosterSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRosterSegmentStore(
    var items: MutableMap<String, StrictRosterSegment>,
    var order: MutableList<String>,
) : StrictRosterSegmentStore {
    override fun find(code: String): StrictRosterSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRosterSegment) {
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

    override fun list(): MutableList<StrictRosterSegment> {
        val out = mutableListOf<StrictRosterSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRosterSegmentStore(): StrictRosterSegmentStore {
    return MemoryStrictRosterSegmentStore(mutableMapOf<String, StrictRosterSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRosterSegmentStore(
    var inner: StrictRosterSegmentStore,
    var cache: TtlCache<StrictRosterSegment>,
    var hits: Int,
    var misses: Int,
) : StrictRosterSegmentStore {
    override fun find(code: String): StrictRosterSegment? {
        val hit = (cache.get(code) as StrictRosterSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRosterSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRosterSegment> = inner.list()
}

fun newCachedStrictRosterSegmentStore(inner: StrictRosterSegmentStore, ttl: Int, clock: Clock): StrictRosterSegmentStore {
    return CachedStrictRosterSegmentStore(
        inner = inner,
        cache = newTtlCache<StrictRosterSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict roster segments. Empty fields match all. */
data class StrictRosterSegmentQuery(
    var source: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRosterSegment): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRosterSegment>): MutableList<StrictRosterSegment> {
        val matched = filter(items) { candidate: StrictRosterSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRosterSegments(store: StrictRosterSegmentStore, query: StrictRosterSegmentQuery): MutableList<StrictRosterSegment> {
    return query.apply(store.list())
}
