package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled locker claims by code. */
interface ScheduledLockerClaimStore {
    fun find(code: String): ScheduledLockerClaim?
    fun save(item: ScheduledLockerClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLockerClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLockerClaimStore(
    var items: MutableMap<String, ScheduledLockerClaim>,
    var order: MutableList<String>,
) : ScheduledLockerClaimStore {
    override fun find(code: String): ScheduledLockerClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLockerClaim) {
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

    override fun list(): MutableList<ScheduledLockerClaim> {
        val out = mutableListOf<ScheduledLockerClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLockerClaimStore(): ScheduledLockerClaimStore {
    return MemoryScheduledLockerClaimStore(
        mutableMapOf<String, ScheduledLockerClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLockerClaimStore(
    var inner: ScheduledLockerClaimStore,
    var cache: TtlCache<ScheduledLockerClaim>,
    var hits: Int,
    var misses: Int,
) : ScheduledLockerClaimStore {
    override fun find(code: String): ScheduledLockerClaim? {
        val hit = (cache.get(code) as ScheduledLockerClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLockerClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLockerClaim> = inner.list()
}

fun newCachedScheduledLockerClaimStore(inner: ScheduledLockerClaimStore, ttl: Int, clock: Clock): ScheduledLockerClaimStore {
    return CachedScheduledLockerClaimStore(
        inner = inner,
        cache = newTtlCache<ScheduledLockerClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled locker claims. Empty fields match all. */
data class ScheduledLockerClaimQuery(
    var category: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLockerClaim): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLockerClaim>): MutableList<ScheduledLockerClaim> {
        val matched = filter(items) { candidate: ScheduledLockerClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLockerClaims(store: ScheduledLockerClaimStore, query: ScheduledLockerClaimQuery): MutableList<ScheduledLockerClaim> {
    return query.apply(store.list())
}
