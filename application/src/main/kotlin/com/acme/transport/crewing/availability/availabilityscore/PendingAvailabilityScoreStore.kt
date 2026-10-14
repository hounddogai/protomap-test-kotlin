package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending availability scores by code. */
interface PendingAvailabilityScoreStore {
    fun find(code: String): PendingAvailabilityScore?
    fun save(item: PendingAvailabilityScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAvailabilityScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAvailabilityScoreStore(
    var items: MutableMap<String, PendingAvailabilityScore>,
    var order: MutableList<String>,
) : PendingAvailabilityScoreStore {
    override fun find(code: String): PendingAvailabilityScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAvailabilityScore) {
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

    override fun list(): MutableList<PendingAvailabilityScore> {
        val out = mutableListOf<PendingAvailabilityScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAvailabilityScoreStore(): PendingAvailabilityScoreStore {
    return MemoryPendingAvailabilityScoreStore(
        mutableMapOf<String, PendingAvailabilityScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAvailabilityScoreStore(
    var inner: PendingAvailabilityScoreStore,
    var cache: TtlCache<PendingAvailabilityScore>,
    var hits: Int,
    var misses: Int,
) : PendingAvailabilityScoreStore {
    override fun find(code: String): PendingAvailabilityScore? {
        val hit = (cache.get(code) as PendingAvailabilityScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAvailabilityScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAvailabilityScore> = inner.list()
}

fun newCachedPendingAvailabilityScoreStore(inner: PendingAvailabilityScoreStore, ttl: Int, clock: Clock): PendingAvailabilityScoreStore {
    return CachedPendingAvailabilityScoreStore(
        inner = inner,
        cache = newTtlCache<PendingAvailabilityScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending availability scores. Empty fields match all. */
data class PendingAvailabilityScoreQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAvailabilityScore): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<PendingAvailabilityScore>): MutableList<PendingAvailabilityScore> {
        val matched = filter(items) { candidate: PendingAvailabilityScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAvailabilityScores(store: PendingAvailabilityScoreStore, query: PendingAvailabilityScoreQuery): MutableList<PendingAvailabilityScore> {
    return query.apply(store.list())
}
