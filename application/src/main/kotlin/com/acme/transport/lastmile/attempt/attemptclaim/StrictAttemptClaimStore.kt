package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict attempt claims by code. */
interface StrictAttemptClaimStore {
    fun find(code: String): StrictAttemptClaim?
    fun save(item: StrictAttemptClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAttemptClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAttemptClaimStore(
    var items: MutableMap<String, StrictAttemptClaim>,
    var order: MutableList<String>,
) : StrictAttemptClaimStore {
    override fun find(code: String): StrictAttemptClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAttemptClaim) {
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

    override fun list(): MutableList<StrictAttemptClaim> {
        val out = mutableListOf<StrictAttemptClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAttemptClaimStore(): StrictAttemptClaimStore {
    return MemoryStrictAttemptClaimStore(mutableMapOf<String, StrictAttemptClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAttemptClaimStore(
    var inner: StrictAttemptClaimStore,
    var cache: TtlCache<StrictAttemptClaim>,
    var hits: Int,
    var misses: Int,
) : StrictAttemptClaimStore {
    override fun find(code: String): StrictAttemptClaim? {
        val hit = (cache.get(code) as StrictAttemptClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAttemptClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAttemptClaim> = inner.list()
}

fun newCachedStrictAttemptClaimStore(inner: StrictAttemptClaimStore, ttl: Int, clock: Clock): StrictAttemptClaimStore {
    return CachedStrictAttemptClaimStore(
        inner = inner,
        cache = newTtlCache<StrictAttemptClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict attempt claims. Empty fields match all. */
data class StrictAttemptClaimQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAttemptClaim): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAttemptClaim>): MutableList<StrictAttemptClaim> {
        val matched = filter(items) { candidate: StrictAttemptClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAttemptClaims(store: StrictAttemptClaimStore, query: StrictAttemptClaimQuery): MutableList<StrictAttemptClaim> {
    return query.apply(store.list())
}
