package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return scores by code. */
interface LeaseReturnScoreStore {
    fun find(code: String): LeaseReturnScore?
    fun save(item: LeaseReturnScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnScoreStore(
    var items: MutableMap<String, LeaseReturnScore>,
    var order: MutableList<String>,
) : LeaseReturnScoreStore {
    override fun find(code: String): LeaseReturnScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnScore) {
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

    override fun list(): MutableList<LeaseReturnScore> {
        val out = mutableListOf<LeaseReturnScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnScoreStore(): LeaseReturnScoreStore {
    return MemoryLeaseReturnScoreStore(mutableMapOf<String, LeaseReturnScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnScoreStore(
    var inner: LeaseReturnScoreStore,
    var cache: TtlCache<LeaseReturnScore>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnScoreStore {
    override fun find(code: String): LeaseReturnScore? {
        val hit = (cache.get(code) as LeaseReturnScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnScore> = inner.list()
}

fun newCachedLeaseReturnScoreStore(inner: LeaseReturnScoreStore, ttl: Int, clock: Clock): LeaseReturnScoreStore {
    return CachedLeaseReturnScoreStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return scores. Empty fields match all. */
data class LeaseReturnScoreQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnScore): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnScore>): MutableList<LeaseReturnScore> {
        val matched = filter(items) { candidate: LeaseReturnScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnScores(store: LeaseReturnScoreStore, query: LeaseReturnScoreQuery): MutableList<LeaseReturnScore> {
    return query.apply(store.list())
}
