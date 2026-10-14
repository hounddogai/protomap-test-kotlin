package com.acme.transport.safety.incident.globalincidentproposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary incident claims by code. */
interface PrimaryIncidentClaimStore {
    fun find(code: String): PrimaryIncidentClaim?
    fun save(item: PrimaryIncidentClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryIncidentClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryIncidentClaimStore(
    var items: MutableMap<String, PrimaryIncidentClaim>,
    var order: MutableList<String>,
) : PrimaryIncidentClaimStore {
    override fun find(code: String): PrimaryIncidentClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryIncidentClaim) {
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

    override fun list(): MutableList<PrimaryIncidentClaim> {
        val out = mutableListOf<PrimaryIncidentClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryIncidentClaimStore(): PrimaryIncidentClaimStore {
    return MemoryPrimaryIncidentClaimStore(
        mutableMapOf<String, PrimaryIncidentClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryIncidentClaimStore(
    var inner: PrimaryIncidentClaimStore,
    var cache: TtlCache<PrimaryIncidentClaim>,
    var hits: Int,
    var misses: Int,
) : PrimaryIncidentClaimStore {
    override fun find(code: String): PrimaryIncidentClaim? {
        val hit = (cache.get(code) as PrimaryIncidentClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryIncidentClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryIncidentClaim> = inner.list()
}

fun newCachedPrimaryIncidentClaimStore(inner: PrimaryIncidentClaimStore, ttl: Int, clock: Clock): PrimaryIncidentClaimStore {
    return CachedPrimaryIncidentClaimStore(
        inner = inner,
        cache = newTtlCache<PrimaryIncidentClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary incident claims. Empty fields match all. */
data class PrimaryIncidentClaimQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryIncidentClaim): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryIncidentClaim>): MutableList<PrimaryIncidentClaim> {
        val matched = filter(items) { candidate: PrimaryIncidentClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryIncidentClaims(store: PrimaryIncidentClaimStore, query: PrimaryIncidentClaimQuery): MutableList<PrimaryIncidentClaim> {
    return query.apply(store.list())
}
