package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending locker claims by code. */
interface PendingLockerClaimStore {
    fun find(code: String): PendingLockerClaim?
    fun save(item: PendingLockerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLockerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLockerClaimStore(
    var items: MutableMap<String, PendingLockerClaim>,
    var order: MutableList<String>,
) : PendingLockerClaimStore {
    override fun find(code: String): PendingLockerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLockerClaim) {
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

    override fun list(): MutableList<PendingLockerClaim> {
        val out = mutableListOf<PendingLockerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLockerClaimStore(): PendingLockerClaimStore {
    return MemoryPendingLockerClaimStore(mutableMapOf<String, PendingLockerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLockerClaimStore(
    var inner: PendingLockerClaimStore,
    var cache: TtlCache<PendingLockerClaim>,
    var hits: Int,
    var misses: Int,
) : PendingLockerClaimStore {
    override fun find(code: String): PendingLockerClaim? {
        val hit = (cache.get(code) as PendingLockerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLockerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLockerClaim> = inner.list()
}

fun newCachedPendingLockerClaimStore(inner: PendingLockerClaimStore, ttl: Int, clock: Clock): PendingLockerClaimStore {
    return CachedPendingLockerClaimStore(
        inner = inner,
        cache = newTtlCache<PendingLockerClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending locker claims. Empty fields match all. */
data class PendingLockerClaimQuery(
    var channel: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLockerClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLockerClaim>): MutableList<PendingLockerClaim> {
        val matched = filter(items) { candidate: PendingLockerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLockerClaims(store: PendingLockerClaimStore, query: PendingLockerClaimQuery): MutableList<PendingLockerClaim> {
    return query.apply(store.list())
}
