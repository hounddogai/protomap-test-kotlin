package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary capacity scores by code. */
interface PrimaryCapacityScoreStore {
    fun find(code: String): PrimaryCapacityScore?
    fun save(item: PrimaryCapacityScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCapacityScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCapacityScoreStore(
    var items: MutableMap<String, PrimaryCapacityScore>,
    var order: MutableList<String>,
) : PrimaryCapacityScoreStore {
    override fun find(code: String): PrimaryCapacityScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCapacityScore) {
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

    override fun list(): MutableList<PrimaryCapacityScore> {
        val out = mutableListOf<PrimaryCapacityScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCapacityScoreStore(): PrimaryCapacityScoreStore {
    return MemoryPrimaryCapacityScoreStore(
        mutableMapOf<String, PrimaryCapacityScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCapacityScoreStore(
    var inner: PrimaryCapacityScoreStore,
    var cache: TtlCache<PrimaryCapacityScore>,
    var hits: Int,
    var misses: Int,
) : PrimaryCapacityScoreStore {
    override fun find(code: String): PrimaryCapacityScore? {
        val hit = (cache.get(code) as PrimaryCapacityScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCapacityScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCapacityScore> = inner.list()
}

fun newCachedPrimaryCapacityScoreStore(inner: PrimaryCapacityScoreStore, ttl: Int, clock: Clock): PrimaryCapacityScoreStore {
    return CachedPrimaryCapacityScoreStore(
        inner = inner,
        cache = newTtlCache<PrimaryCapacityScore>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary capacity scores. Empty fields match all. */
data class PrimaryCapacityScoreQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCapacityScore): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<PrimaryCapacityScore>): MutableList<PrimaryCapacityScore> {
        val matched = filter(items) { candidate: PrimaryCapacityScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCapacityScores(store: PrimaryCapacityScoreStore, query: PrimaryCapacityScoreQuery): MutableList<PrimaryCapacityScore> {
    return query.apply(store.list())
}
