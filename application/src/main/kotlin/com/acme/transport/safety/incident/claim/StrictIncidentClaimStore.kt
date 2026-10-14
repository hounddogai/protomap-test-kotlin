package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps strict incident claims by code. */
interface StrictIncidentClaimStore {
    fun find(code: String): StrictIncidentClaim?
    fun save(item: StrictIncidentClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictIncidentClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictIncidentClaimStore(
    var items: MutableMap<String, StrictIncidentClaim>,
    var order: MutableList<String>,
) : StrictIncidentClaimStore {
    override fun find(code: String): StrictIncidentClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictIncidentClaim) {
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

    override fun list(): MutableList<StrictIncidentClaim> {
        val out = mutableListOf<StrictIncidentClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictIncidentClaimStore(): StrictIncidentClaimStore {
    return MemoryStrictIncidentClaimStore(mutableMapOf<String, StrictIncidentClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictIncidentClaimStore(
    var inner: StrictIncidentClaimStore,
    var cache: TtlCache<StrictIncidentClaim>,
    var hits: Int,
    var misses: Int,
) : StrictIncidentClaimStore {
    override fun find(code: String): StrictIncidentClaim? {
        val hit = (cache.get(code) as StrictIncidentClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictIncidentClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictIncidentClaim> = inner.list()
}

fun newCachedStrictIncidentClaimStore(inner: StrictIncidentClaimStore, ttl: Int, clock: Clock): StrictIncidentClaimStore {
    return CachedStrictIncidentClaimStore(
        inner = inner,
        cache = newTtlCache<StrictIncidentClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict incident claims. Empty fields match all. */
data class StrictIncidentClaimQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictIncidentClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<StrictIncidentClaim>): MutableList<StrictIncidentClaim> {
        val matched = filter(items) { candidate: StrictIncidentClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictIncidentClaims(store: StrictIncidentClaimStore, query: StrictIncidentClaimQuery): MutableList<StrictIncidentClaim> {
    return query.apply(store.list())
}
