package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff segments by code. */
interface CutoffSegmentStore {
    fun find(code: String): CutoffSegment?
    fun save(item: CutoffSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffSegmentStore(
    var items: MutableMap<String, CutoffSegment>,
    var order: MutableList<String>,
) : CutoffSegmentStore {
    override fun find(code: String): CutoffSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffSegment) {
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

    override fun list(): MutableList<CutoffSegment> {
        val out = mutableListOf<CutoffSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffSegmentStore(): CutoffSegmentStore {
    return MemoryCutoffSegmentStore(mutableMapOf<String, CutoffSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffSegmentStore(
    var inner: CutoffSegmentStore,
    var cache: TtlCache<CutoffSegment>,
    var hits: Int,
    var misses: Int,
) : CutoffSegmentStore {
    override fun find(code: String): CutoffSegment? {
        val hit = (cache.get(code) as CutoffSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffSegment> = inner.list()
}

fun newCachedCutoffSegmentStore(inner: CutoffSegmentStore, ttl: Int, clock: Clock): CutoffSegmentStore {
    return CachedCutoffSegmentStore(
        inner = inner,
        cache = newTtlCache<CutoffSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff segments. Empty fields match all. */
data class CutoffSegmentQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffSegment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<CutoffSegment>): MutableList<CutoffSegment> {
        val matched = filter(items) { candidate: CutoffSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffSegments(store: CutoffSegmentStore, query: CutoffSegmentQuery): MutableList<CutoffSegment> {
    return query.apply(store.list())
}
