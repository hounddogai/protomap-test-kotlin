package com.acme.transport.crewing.roster.segment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional roster segments by code. */
interface RegionalRosterSegmentStore {
    fun find(code: String): RegionalRosterSegment?
    fun save(item: RegionalRosterSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRosterSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRosterSegmentStore(
    var items: MutableMap<String, RegionalRosterSegment>,
    var order: MutableList<String>,
) : RegionalRosterSegmentStore {
    override fun find(code: String): RegionalRosterSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRosterSegment) {
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

    override fun list(): MutableList<RegionalRosterSegment> {
        val out = mutableListOf<RegionalRosterSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRosterSegmentStore(): RegionalRosterSegmentStore {
    return MemoryRegionalRosterSegmentStore(
        mutableMapOf<String, RegionalRosterSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRosterSegmentStore(
    var inner: RegionalRosterSegmentStore,
    var cache: TtlCache<RegionalRosterSegment>,
    var hits: Int,
    var misses: Int,
) : RegionalRosterSegmentStore {
    override fun find(code: String): RegionalRosterSegment? {
        val hit = (cache.get(code) as RegionalRosterSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRosterSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRosterSegment> = inner.list()
}

fun newCachedRegionalRosterSegmentStore(inner: RegionalRosterSegmentStore, ttl: Int, clock: Clock): RegionalRosterSegmentStore {
    return CachedRegionalRosterSegmentStore(
        inner = inner,
        cache = newTtlCache<RegionalRosterSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional roster segments. Empty fields match all. */
data class RegionalRosterSegmentQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRosterSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalRosterSegment>): MutableList<RegionalRosterSegment> {
        val matched = filter(items) { candidate: RegionalRosterSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRosterSegments(store: RegionalRosterSegmentStore, query: RegionalRosterSegmentQuery): MutableList<RegionalRosterSegment> {
    return query.apply(store.list())
}
