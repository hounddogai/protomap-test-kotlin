package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default lease return claims by code. */
interface DefaultLeaseReturnClaimStore {
    fun find(code: String): DefaultLeaseReturnClaim?
    fun save(item: DefaultLeaseReturnClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultLeaseReturnClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultLeaseReturnClaimStore(
    var items: MutableMap<String, DefaultLeaseReturnClaim>,
    var order: MutableList<String>,
) : DefaultLeaseReturnClaimStore {
    override fun find(code: String): DefaultLeaseReturnClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultLeaseReturnClaim) {
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

    override fun list(): MutableList<DefaultLeaseReturnClaim> {
        val out = mutableListOf<DefaultLeaseReturnClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultLeaseReturnClaimStore(): DefaultLeaseReturnClaimStore {
    return MemoryDefaultLeaseReturnClaimStore(
        mutableMapOf<String, DefaultLeaseReturnClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultLeaseReturnClaimStore(
    var inner: DefaultLeaseReturnClaimStore,
    var cache: TtlCache<DefaultLeaseReturnClaim>,
    var hits: Int,
    var misses: Int,
) : DefaultLeaseReturnClaimStore {
    override fun find(code: String): DefaultLeaseReturnClaim? {
        val hit = (cache.get(code) as DefaultLeaseReturnClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultLeaseReturnClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultLeaseReturnClaim> = inner.list()
}

fun newCachedDefaultLeaseReturnClaimStore(inner: DefaultLeaseReturnClaimStore, ttl: Int, clock: Clock): DefaultLeaseReturnClaimStore {
    return CachedDefaultLeaseReturnClaimStore(
        inner = inner,
        cache = newTtlCache<DefaultLeaseReturnClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default lease return claims. Empty fields match all. */
data class DefaultLeaseReturnClaimQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultLeaseReturnClaim): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultLeaseReturnClaim>): MutableList<DefaultLeaseReturnClaim> {
        val matched = filter(items) { candidate: DefaultLeaseReturnClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultLeaseReturnClaims(store: DefaultLeaseReturnClaimStore, query: DefaultLeaseReturnClaimQuery): MutableList<DefaultLeaseReturnClaim> {
    return query.apply(store.list())
}
