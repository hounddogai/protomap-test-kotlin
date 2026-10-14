package com.acme.transport.network.cutoff.regionalcutoffsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending cutoff segments by code. */
interface PendingCutoffSegmentStore {
    fun find(code: String): PendingCutoffSegment?
    fun save(item: PendingCutoffSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCutoffSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCutoffSegmentStore(
    var items: MutableMap<String, PendingCutoffSegment>,
    var order: MutableList<String>,
) : PendingCutoffSegmentStore {
    override fun find(code: String): PendingCutoffSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCutoffSegment) {
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

    override fun list(): MutableList<PendingCutoffSegment> {
        val out = mutableListOf<PendingCutoffSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCutoffSegmentStore(): PendingCutoffSegmentStore {
    return MemoryPendingCutoffSegmentStore(
        mutableMapOf<String, PendingCutoffSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCutoffSegmentStore(
    var inner: PendingCutoffSegmentStore,
    var cache: TtlCache<PendingCutoffSegment>,
    var hits: Int,
    var misses: Int,
) : PendingCutoffSegmentStore {
    override fun find(code: String): PendingCutoffSegment? {
        val hit = (cache.get(code) as PendingCutoffSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCutoffSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCutoffSegment> = inner.list()
}

fun newCachedPendingCutoffSegmentStore(inner: PendingCutoffSegmentStore, ttl: Int, clock: Clock): PendingCutoffSegmentStore {
    return CachedPendingCutoffSegmentStore(
        inner = inner,
        cache = newTtlCache<PendingCutoffSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending cutoff segments. Empty fields match all. */
data class PendingCutoffSegmentQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCutoffSegment): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCutoffSegment>): MutableList<PendingCutoffSegment> {
        val matched = filter(items) { candidate: PendingCutoffSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCutoffSegments(store: PendingCutoffSegmentStore, query: PendingCutoffSegmentQuery): MutableList<PendingCutoffSegment> {
    return query.apply(store.list())
}
