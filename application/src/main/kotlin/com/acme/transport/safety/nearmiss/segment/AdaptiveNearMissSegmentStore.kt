package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive near miss segments by code. */
interface AdaptiveNearMissSegmentStore {
    fun find(code: String): AdaptiveNearMissSegment?
    fun save(item: AdaptiveNearMissSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNearMissSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNearMissSegmentStore(
    var items: MutableMap<String, AdaptiveNearMissSegment>,
    var order: MutableList<String>,
) : AdaptiveNearMissSegmentStore {
    override fun find(code: String): AdaptiveNearMissSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNearMissSegment) {
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

    override fun list(): MutableList<AdaptiveNearMissSegment> {
        val out = mutableListOf<AdaptiveNearMissSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNearMissSegmentStore(): AdaptiveNearMissSegmentStore {
    return MemoryAdaptiveNearMissSegmentStore(
        mutableMapOf<String, AdaptiveNearMissSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNearMissSegmentStore(
    var inner: AdaptiveNearMissSegmentStore,
    var cache: TtlCache<AdaptiveNearMissSegment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNearMissSegmentStore {
    override fun find(code: String): AdaptiveNearMissSegment? {
        val hit = (cache.get(code) as AdaptiveNearMissSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNearMissSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNearMissSegment> = inner.list()
}

fun newCachedAdaptiveNearMissSegmentStore(inner: AdaptiveNearMissSegmentStore, ttl: Int, clock: Clock): AdaptiveNearMissSegmentStore {
    return CachedAdaptiveNearMissSegmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNearMissSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive near miss segments. Empty fields match all. */
data class AdaptiveNearMissSegmentQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNearMissSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveNearMissSegment>): MutableList<AdaptiveNearMissSegment> {
        val matched = filter(items) { candidate: AdaptiveNearMissSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNearMissSegments(store: AdaptiveNearMissSegmentStore, query: AdaptiveNearMissSegmentQuery): MutableList<AdaptiveNearMissSegment> {
    return query.apply(store.list())
}
