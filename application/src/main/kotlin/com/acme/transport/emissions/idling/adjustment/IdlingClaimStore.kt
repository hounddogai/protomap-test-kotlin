package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling claims by code. */
interface IdlingClaimStore {
    fun find(code: String): IdlingClaim?
    fun save(item: IdlingClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingClaimStore(
    var items: MutableMap<String, IdlingClaim>,
    var order: MutableList<String>,
) : IdlingClaimStore {
    override fun find(code: String): IdlingClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingClaim) {
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

    override fun list(): MutableList<IdlingClaim> {
        val out = mutableListOf<IdlingClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingClaimStore(): IdlingClaimStore {
    return MemoryIdlingClaimStore(mutableMapOf<String, IdlingClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingClaimStore(
    var inner: IdlingClaimStore,
    var cache: TtlCache<IdlingClaim>,
    var hits: Int,
    var misses: Int,
) : IdlingClaimStore {
    override fun find(code: String): IdlingClaim? {
        val hit = (cache.get(code) as IdlingClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingClaim> = inner.list()
}

fun newCachedIdlingClaimStore(inner: IdlingClaimStore, ttl: Int, clock: Clock): IdlingClaimStore {
    return CachedIdlingClaimStore(
        inner = inner,
        cache = newTtlCache<IdlingClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling claims. Empty fields match all. */
data class IdlingClaimQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingClaim>): MutableList<IdlingClaim> {
        val matched = filter(items) { candidate: IdlingClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingClaims(store: IdlingClaimStore, query: IdlingClaimQuery): MutableList<IdlingClaim> {
    return query.apply(store.list())
}
