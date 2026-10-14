package com.acme.transport.terminal.dwell.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual dwell segments by code. */
interface ManualDwellSegmentStore {
    fun find(code: String): ManualDwellSegment?
    fun save(item: ManualDwellSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDwellSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDwellSegmentStore(
    var items: MutableMap<String, ManualDwellSegment>,
    var order: MutableList<String>,
) : ManualDwellSegmentStore {
    override fun find(code: String): ManualDwellSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDwellSegment) {
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

    override fun list(): MutableList<ManualDwellSegment> {
        val out = mutableListOf<ManualDwellSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDwellSegmentStore(): ManualDwellSegmentStore {
    return MemoryManualDwellSegmentStore(mutableMapOf<String, ManualDwellSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDwellSegmentStore(
    var inner: ManualDwellSegmentStore,
    var cache: TtlCache<ManualDwellSegment>,
    var hits: Int,
    var misses: Int,
) : ManualDwellSegmentStore {
    override fun find(code: String): ManualDwellSegment? {
        val hit = (cache.get(code) as ManualDwellSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDwellSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDwellSegment> = inner.list()
}

fun newCachedManualDwellSegmentStore(inner: ManualDwellSegmentStore, ttl: Int, clock: Clock): ManualDwellSegmentStore {
    return CachedManualDwellSegmentStore(
        inner = inner,
        cache = newTtlCache<ManualDwellSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual dwell segments. Empty fields match all. */
data class ManualDwellSegmentQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDwellSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDwellSegment>): MutableList<ManualDwellSegment> {
        val matched = filter(items) { candidate: ManualDwellSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDwellSegments(store: ManualDwellSegmentStore, query: ManualDwellSegmentQuery): MutableList<ManualDwellSegment> {
    return query.apply(store.list())
}
