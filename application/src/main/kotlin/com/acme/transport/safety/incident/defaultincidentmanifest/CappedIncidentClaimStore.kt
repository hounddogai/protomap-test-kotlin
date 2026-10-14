package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped incident claims by code. */
interface CappedIncidentClaimStore {
    fun find(code: String): CappedIncidentClaim?
    fun save(item: CappedIncidentClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedIncidentClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedIncidentClaimStore(
    var items: MutableMap<String, CappedIncidentClaim>,
    var order: MutableList<String>,
) : CappedIncidentClaimStore {
    override fun find(code: String): CappedIncidentClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedIncidentClaim) {
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

    override fun list(): MutableList<CappedIncidentClaim> {
        val out = mutableListOf<CappedIncidentClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedIncidentClaimStore(): CappedIncidentClaimStore {
    return MemoryCappedIncidentClaimStore(mutableMapOf<String, CappedIncidentClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedIncidentClaimStore(
    var inner: CappedIncidentClaimStore,
    var cache: TtlCache<CappedIncidentClaim>,
    var hits: Int,
    var misses: Int,
) : CappedIncidentClaimStore {
    override fun find(code: String): CappedIncidentClaim? {
        val hit = (cache.get(code) as CappedIncidentClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedIncidentClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedIncidentClaim> = inner.list()
}

fun newCachedCappedIncidentClaimStore(inner: CappedIncidentClaimStore, ttl: Int, clock: Clock): CappedIncidentClaimStore {
    return CachedCappedIncidentClaimStore(
        inner = inner,
        cache = newTtlCache<CappedIncidentClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped incident claims. Empty fields match all. */
data class CappedIncidentClaimQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedIncidentClaim): Boolean {
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

    fun apply(items: MutableList<CappedIncidentClaim>): MutableList<CappedIncidentClaim> {
        val matched = filter(items) { candidate: CappedIncidentClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedIncidentClaims(store: CappedIncidentClaimStore, query: CappedIncidentClaimQuery): MutableList<CappedIncidentClaim> {
    return query.apply(store.list())
}
