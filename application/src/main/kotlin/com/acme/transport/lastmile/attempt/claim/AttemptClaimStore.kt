package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt claims by code. */
interface AttemptClaimStore {
    fun find(code: String): AttemptClaim?
    fun save(item: AttemptClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptClaimStore(
    var items: MutableMap<String, AttemptClaim>,
    var order: MutableList<String>,
) : AttemptClaimStore {
    override fun find(code: String): AttemptClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptClaim) {
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

    override fun list(): MutableList<AttemptClaim> {
        val out = mutableListOf<AttemptClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptClaimStore(): AttemptClaimStore {
    return MemoryAttemptClaimStore(mutableMapOf<String, AttemptClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptClaimStore(
    var inner: AttemptClaimStore,
    var cache: TtlCache<AttemptClaim>,
    var hits: Int,
    var misses: Int,
) : AttemptClaimStore {
    override fun find(code: String): AttemptClaim? {
        val hit = (cache.get(code) as AttemptClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptClaim> = inner.list()
}

fun newCachedAttemptClaimStore(inner: AttemptClaimStore, ttl: Int, clock: Clock): AttemptClaimStore {
    return CachedAttemptClaimStore(
        inner = inner,
        cache = newTtlCache<AttemptClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt claims. Empty fields match all. */
data class AttemptClaimQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptClaim>): MutableList<AttemptClaim> {
        val matched = filter(items) { candidate: AttemptClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptClaims(store: AttemptClaimStore, query: AttemptClaimQuery): MutableList<AttemptClaim> {
    return query.apply(store.list())
}
