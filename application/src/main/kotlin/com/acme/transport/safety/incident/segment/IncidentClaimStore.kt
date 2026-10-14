package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident claims by code. */
interface IncidentClaimStore {
    fun find(code: String): IncidentClaim?
    fun save(item: IncidentClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentClaimStore(
    var items: MutableMap<String, IncidentClaim>,
    var order: MutableList<String>,
) : IncidentClaimStore {
    override fun find(code: String): IncidentClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentClaim) {
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

    override fun list(): MutableList<IncidentClaim> {
        val out = mutableListOf<IncidentClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentClaimStore(): IncidentClaimStore {
    return MemoryIncidentClaimStore(mutableMapOf<String, IncidentClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentClaimStore(
    var inner: IncidentClaimStore,
    var cache: TtlCache<IncidentClaim>,
    var hits: Int,
    var misses: Int,
) : IncidentClaimStore {
    override fun find(code: String): IncidentClaim? {
        val hit = (cache.get(code) as IncidentClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentClaim> = inner.list()
}

fun newCachedIncidentClaimStore(inner: IncidentClaimStore, ttl: Int, clock: Clock): IncidentClaimStore {
    return CachedIncidentClaimStore(
        inner = inner,
        cache = newTtlCache<IncidentClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident claims. Empty fields match all. */
data class IncidentClaimQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentClaim>): MutableList<IncidentClaim> {
        val matched = filter(items) { candidate: IncidentClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentClaims(store: IncidentClaimStore, query: IncidentClaimQuery): MutableList<IncidentClaim> {
    return query.apply(store.list())
}
