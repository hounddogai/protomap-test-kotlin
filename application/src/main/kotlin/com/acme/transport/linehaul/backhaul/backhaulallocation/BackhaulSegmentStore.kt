package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul segments by code. */
interface BackhaulSegmentStore {
    fun find(code: String): BackhaulSegment?
    fun save(item: BackhaulSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulSegmentStore(
    var items: MutableMap<String, BackhaulSegment>,
    var order: MutableList<String>,
) : BackhaulSegmentStore {
    override fun find(code: String): BackhaulSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulSegment) {
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

    override fun list(): MutableList<BackhaulSegment> {
        val out = mutableListOf<BackhaulSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulSegmentStore(): BackhaulSegmentStore {
    return MemoryBackhaulSegmentStore(mutableMapOf<String, BackhaulSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulSegmentStore(
    var inner: BackhaulSegmentStore,
    var cache: TtlCache<BackhaulSegment>,
    var hits: Int,
    var misses: Int,
) : BackhaulSegmentStore {
    override fun find(code: String): BackhaulSegment? {
        val hit = (cache.get(code) as BackhaulSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulSegment> = inner.list()
}

fun newCachedBackhaulSegmentStore(inner: BackhaulSegmentStore, ttl: Int, clock: Clock): BackhaulSegmentStore {
    return CachedBackhaulSegmentStore(
        inner = inner,
        cache = newTtlCache<BackhaulSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul segments. Empty fields match all. */
data class BackhaulSegmentQuery(
    var unit: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulSegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulSegment>): MutableList<BackhaulSegment> {
        val matched = filter(items) { candidate: BackhaulSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulSegments(store: BackhaulSegmentStore, query: BackhaulSegmentQuery): MutableList<BackhaulSegment> {
    return query.apply(store.list())
}
