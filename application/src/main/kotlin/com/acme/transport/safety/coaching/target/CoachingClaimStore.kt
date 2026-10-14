package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching claims by code. */
interface CoachingClaimStore {
    fun find(code: String): CoachingClaim?
    fun save(item: CoachingClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingClaimStore(
    var items: MutableMap<String, CoachingClaim>,
    var order: MutableList<String>,
) : CoachingClaimStore {
    override fun find(code: String): CoachingClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingClaim) {
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

    override fun list(): MutableList<CoachingClaim> {
        val out = mutableListOf<CoachingClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingClaimStore(): CoachingClaimStore {
    return MemoryCoachingClaimStore(mutableMapOf<String, CoachingClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingClaimStore(
    var inner: CoachingClaimStore,
    var cache: TtlCache<CoachingClaim>,
    var hits: Int,
    var misses: Int,
) : CoachingClaimStore {
    override fun find(code: String): CoachingClaim? {
        val hit = (cache.get(code) as CoachingClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingClaim> = inner.list()
}

fun newCachedCoachingClaimStore(inner: CoachingClaimStore, ttl: Int, clock: Clock): CoachingClaimStore {
    return CachedCoachingClaimStore(
        inner = inner,
        cache = newTtlCache<CoachingClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching claims. Empty fields match all. */
data class CoachingClaimQuery(
    var source: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingClaim): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingClaim>): MutableList<CoachingClaim> {
        val matched = filter(items) { candidate: CoachingClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingClaims(store: CoachingClaimStore, query: CoachingClaimQuery): MutableList<CoachingClaim> {
    return query.apply(store.list())
}
