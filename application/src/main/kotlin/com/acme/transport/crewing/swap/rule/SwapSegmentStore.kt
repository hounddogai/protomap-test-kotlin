package com.acme.transport.crewing.swap.rule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap segments by code. */
interface SwapSegmentStore {
    fun find(code: String): SwapSegment?
    fun save(item: SwapSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapSegmentStore(
    var items: MutableMap<String, SwapSegment>,
    var order: MutableList<String>,
) : SwapSegmentStore {
    override fun find(code: String): SwapSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapSegment) {
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

    override fun list(): MutableList<SwapSegment> {
        val out = mutableListOf<SwapSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapSegmentStore(): SwapSegmentStore {
    return MemorySwapSegmentStore(mutableMapOf<String, SwapSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapSegmentStore(
    var inner: SwapSegmentStore,
    var cache: TtlCache<SwapSegment>,
    var hits: Int,
    var misses: Int,
) : SwapSegmentStore {
    override fun find(code: String): SwapSegment? {
        val hit = (cache.get(code) as SwapSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapSegment> = inner.list()
}

fun newCachedSwapSegmentStore(inner: SwapSegmentStore, ttl: Int, clock: Clock): SwapSegmentStore {
    return CachedSwapSegmentStore(
        inner = inner,
        cache = newTtlCache<SwapSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap segments. Empty fields match all. */
data class SwapSegmentQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapSegment>): MutableList<SwapSegment> {
        val matched = filter(items) { candidate: SwapSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapSegments(store: SwapSegmentStore, query: SwapSegmentQuery): MutableList<SwapSegment> {
    return query.apply(store.list())
}
