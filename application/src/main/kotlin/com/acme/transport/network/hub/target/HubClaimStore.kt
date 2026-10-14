package com.acme.transport.network.hub.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub claims by code. */
interface HubClaimStore {
    fun find(code: String): HubClaim?
    fun save(item: HubClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubClaimStore(
    var items: MutableMap<String, HubClaim>,
    var order: MutableList<String>,
) : HubClaimStore {
    override fun find(code: String): HubClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubClaim) {
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

    override fun list(): MutableList<HubClaim> {
        val out = mutableListOf<HubClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubClaimStore(): HubClaimStore {
    return MemoryHubClaimStore(mutableMapOf<String, HubClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubClaimStore(
    var inner: HubClaimStore,
    var cache: TtlCache<HubClaim>,
    var hits: Int,
    var misses: Int,
) : HubClaimStore {
    override fun find(code: String): HubClaim? {
        val hit = (cache.get(code) as HubClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubClaim> = inner.list()
}

fun newCachedHubClaimStore(inner: HubClaimStore, ttl: Int, clock: Clock): HubClaimStore {
    return CachedHubClaimStore(
        inner = inner,
        cache = newTtlCache<HubClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub claims. Empty fields match all. */
data class HubClaimQuery(
    var unit: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubClaim): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<HubClaim>): MutableList<HubClaim> {
        val matched = filter(items) { candidate: HubClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubClaims(store: HubClaimStore, query: HubClaimQuery): MutableList<HubClaim> = query.apply(store.list())
