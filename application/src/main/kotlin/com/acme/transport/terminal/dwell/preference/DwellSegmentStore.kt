package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell segments by code. */
interface DwellSegmentStore {
    fun find(code: String): DwellSegment?
    fun save(item: DwellSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellSegmentStore(
    var items: MutableMap<String, DwellSegment>,
    var order: MutableList<String>,
) : DwellSegmentStore {
    override fun find(code: String): DwellSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellSegment) {
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

    override fun list(): MutableList<DwellSegment> {
        val out = mutableListOf<DwellSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellSegmentStore(): DwellSegmentStore {
    return MemoryDwellSegmentStore(mutableMapOf<String, DwellSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellSegmentStore(
    var inner: DwellSegmentStore,
    var cache: TtlCache<DwellSegment>,
    var hits: Int,
    var misses: Int,
) : DwellSegmentStore {
    override fun find(code: String): DwellSegment? {
        val hit = (cache.get(code) as DwellSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellSegment> = inner.list()
}

fun newCachedDwellSegmentStore(inner: DwellSegmentStore, ttl: Int, clock: Clock): DwellSegmentStore {
    return CachedDwellSegmentStore(
        inner = inner,
        cache = newTtlCache<DwellSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell segments. Empty fields match all. */
data class DwellSegmentQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellSegment>): MutableList<DwellSegment> {
        val matched = filter(items) { candidate: DwellSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellSegments(store: DwellSegmentStore, query: DwellSegmentQuery): MutableList<DwellSegment> {
    return query.apply(store.list())
}
