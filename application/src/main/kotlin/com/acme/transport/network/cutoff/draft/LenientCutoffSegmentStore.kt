package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient cutoff segments by code. */
interface LenientCutoffSegmentStore {
    fun find(code: String): LenientCutoffSegment?
    fun save(item: LenientCutoffSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCutoffSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCutoffSegmentStore(
    var items: MutableMap<String, LenientCutoffSegment>,
    var order: MutableList<String>,
) : LenientCutoffSegmentStore {
    override fun find(code: String): LenientCutoffSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCutoffSegment) {
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

    override fun list(): MutableList<LenientCutoffSegment> {
        val out = mutableListOf<LenientCutoffSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCutoffSegmentStore(): LenientCutoffSegmentStore {
    return MemoryLenientCutoffSegmentStore(
        mutableMapOf<String, LenientCutoffSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCutoffSegmentStore(
    var inner: LenientCutoffSegmentStore,
    var cache: TtlCache<LenientCutoffSegment>,
    var hits: Int,
    var misses: Int,
) : LenientCutoffSegmentStore {
    override fun find(code: String): LenientCutoffSegment? {
        val hit = (cache.get(code) as LenientCutoffSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCutoffSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCutoffSegment> = inner.list()
}

fun newCachedLenientCutoffSegmentStore(inner: LenientCutoffSegmentStore, ttl: Int, clock: Clock): LenientCutoffSegmentStore {
    return CachedLenientCutoffSegmentStore(
        inner = inner,
        cache = newTtlCache<LenientCutoffSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient cutoff segments. Empty fields match all. */
data class LenientCutoffSegmentQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCutoffSegment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCutoffSegment>): MutableList<LenientCutoffSegment> {
        val matched = filter(items) { candidate: LenientCutoffSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCutoffSegments(store: LenientCutoffSegmentStore, query: LenientCutoffSegmentQuery): MutableList<LenientCutoffSegment> {
    return query.apply(store.list())
}
