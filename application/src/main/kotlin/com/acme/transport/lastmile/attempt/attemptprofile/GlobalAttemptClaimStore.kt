package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global attempt claims by code. */
interface GlobalAttemptClaimStore {
    fun find(code: String): GlobalAttemptClaim?
    fun save(item: GlobalAttemptClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAttemptClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAttemptClaimStore(
    var items: MutableMap<String, GlobalAttemptClaim>,
    var order: MutableList<String>,
) : GlobalAttemptClaimStore {
    override fun find(code: String): GlobalAttemptClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAttemptClaim) {
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

    override fun list(): MutableList<GlobalAttemptClaim> {
        val out = mutableListOf<GlobalAttemptClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAttemptClaimStore(): GlobalAttemptClaimStore {
    return MemoryGlobalAttemptClaimStore(mutableMapOf<String, GlobalAttemptClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAttemptClaimStore(
    var inner: GlobalAttemptClaimStore,
    var cache: TtlCache<GlobalAttemptClaim>,
    var hits: Int,
    var misses: Int,
) : GlobalAttemptClaimStore {
    override fun find(code: String): GlobalAttemptClaim? {
        val hit = (cache.get(code) as GlobalAttemptClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAttemptClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAttemptClaim> = inner.list()
}

fun newCachedGlobalAttemptClaimStore(inner: GlobalAttemptClaimStore, ttl: Int, clock: Clock): GlobalAttemptClaimStore {
    return CachedGlobalAttemptClaimStore(
        inner = inner,
        cache = newTtlCache<GlobalAttemptClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global attempt claims. Empty fields match all. */
data class GlobalAttemptClaimQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAttemptClaim): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAttemptClaim>): MutableList<GlobalAttemptClaim> {
        val matched = filter(items) { candidate: GlobalAttemptClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAttemptClaims(store: GlobalAttemptClaimStore, query: GlobalAttemptClaimQuery): MutableList<GlobalAttemptClaim> {
    return query.apply(store.list())
}
