package com.acme.transport.lastmile.locker.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker claims by code. */
interface LockerClaimStore {
    fun find(code: String): LockerClaim?
    fun save(item: LockerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerClaimStore(
    var items: MutableMap<String, LockerClaim>,
    var order: MutableList<String>,
) : LockerClaimStore {
    override fun find(code: String): LockerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerClaim) {
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

    override fun list(): MutableList<LockerClaim> {
        val out = mutableListOf<LockerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerClaimStore(): LockerClaimStore {
    return MemoryLockerClaimStore(mutableMapOf<String, LockerClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerClaimStore(
    var inner: LockerClaimStore,
    var cache: TtlCache<LockerClaim>,
    var hits: Int,
    var misses: Int,
) : LockerClaimStore {
    override fun find(code: String): LockerClaim? {
        val hit = (cache.get(code) as LockerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerClaim> = inner.list()
}

fun newCachedLockerClaimStore(inner: LockerClaimStore, ttl: Int, clock: Clock): LockerClaimStore {
    return CachedLockerClaimStore(
        inner = inner,
        cache = newTtlCache<LockerClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker claims. Empty fields match all. */
data class LockerClaimQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerClaim>): MutableList<LockerClaim> {
        val matched = filter(items) { candidate: LockerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerClaims(store: LockerClaimStore, query: LockerClaimQuery): MutableList<LockerClaim> {
    return query.apply(store.list())
}
