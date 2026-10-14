package com.acme.transport.crewing.swap.swapmapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback swap segments by code. */
interface FallbackSwapSegmentStore {
    fun find(code: String): FallbackSwapSegment?
    fun save(item: FallbackSwapSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackSwapSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackSwapSegmentStore(
    var items: MutableMap<String, FallbackSwapSegment>,
    var order: MutableList<String>,
) : FallbackSwapSegmentStore {
    override fun find(code: String): FallbackSwapSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackSwapSegment) {
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

    override fun list(): MutableList<FallbackSwapSegment> {
        val out = mutableListOf<FallbackSwapSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackSwapSegmentStore(): FallbackSwapSegmentStore {
    return MemoryFallbackSwapSegmentStore(mutableMapOf<String, FallbackSwapSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackSwapSegmentStore(
    var inner: FallbackSwapSegmentStore,
    var cache: TtlCache<FallbackSwapSegment>,
    var hits: Int,
    var misses: Int,
) : FallbackSwapSegmentStore {
    override fun find(code: String): FallbackSwapSegment? {
        val hit = (cache.get(code) as FallbackSwapSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackSwapSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackSwapSegment> = inner.list()
}

fun newCachedFallbackSwapSegmentStore(inner: FallbackSwapSegmentStore, ttl: Int, clock: Clock): FallbackSwapSegmentStore {
    return CachedFallbackSwapSegmentStore(
        inner = inner,
        cache = newTtlCache<FallbackSwapSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback swap segments. Empty fields match all. */
data class FallbackSwapSegmentQuery(
    var unit: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackSwapSegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackSwapSegment>): MutableList<FallbackSwapSegment> {
        val matched = filter(items) { candidate: FallbackSwapSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackSwapSegments(store: FallbackSwapSegmentStore, query: FallbackSwapSegmentQuery): MutableList<FallbackSwapSegment> {
    return query.apply(store.list())
}
