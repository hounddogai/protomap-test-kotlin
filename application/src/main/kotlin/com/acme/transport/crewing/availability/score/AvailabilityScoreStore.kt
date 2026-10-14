package com.acme.transport.crewing.availability.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability scores by code. */
interface AvailabilityScoreStore {
    fun find(code: String): AvailabilityScore?
    fun save(item: AvailabilityScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityScoreStore(
    var items: MutableMap<String, AvailabilityScore>,
    var order: MutableList<String>,
) : AvailabilityScoreStore {
    override fun find(code: String): AvailabilityScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityScore) {
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

    override fun list(): MutableList<AvailabilityScore> {
        val out = mutableListOf<AvailabilityScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityScoreStore(): AvailabilityScoreStore {
    return MemoryAvailabilityScoreStore(mutableMapOf<String, AvailabilityScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityScoreStore(
    var inner: AvailabilityScoreStore,
    var cache: TtlCache<AvailabilityScore>,
    var hits: Int,
    var misses: Int,
) : AvailabilityScoreStore {
    override fun find(code: String): AvailabilityScore? {
        val hit = (cache.get(code) as AvailabilityScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityScore> = inner.list()
}

fun newCachedAvailabilityScoreStore(inner: AvailabilityScoreStore, ttl: Int, clock: Clock): AvailabilityScoreStore {
    return CachedAvailabilityScoreStore(
        inner = inner,
        cache = newTtlCache<AvailabilityScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability scores. Empty fields match all. */
data class AvailabilityScoreQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityScore): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<AvailabilityScore>): MutableList<AvailabilityScore> {
        val matched = filter(items) { candidate: AvailabilityScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityScores(store: AvailabilityScoreStore, query: AvailabilityScoreQuery): MutableList<AvailabilityScore> {
    return query.apply(store.list())
}
