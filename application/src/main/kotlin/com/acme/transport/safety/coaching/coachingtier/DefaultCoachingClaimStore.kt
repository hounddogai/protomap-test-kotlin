package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default coaching claims by code. */
interface DefaultCoachingClaimStore {
    fun find(code: String): DefaultCoachingClaim?
    fun save(item: DefaultCoachingClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCoachingClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCoachingClaimStore(
    var items: MutableMap<String, DefaultCoachingClaim>,
    var order: MutableList<String>,
) : DefaultCoachingClaimStore {
    override fun find(code: String): DefaultCoachingClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCoachingClaim) {
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

    override fun list(): MutableList<DefaultCoachingClaim> {
        val out = mutableListOf<DefaultCoachingClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCoachingClaimStore(): DefaultCoachingClaimStore {
    return MemoryDefaultCoachingClaimStore(
        mutableMapOf<String, DefaultCoachingClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCoachingClaimStore(
    var inner: DefaultCoachingClaimStore,
    var cache: TtlCache<DefaultCoachingClaim>,
    var hits: Int,
    var misses: Int,
) : DefaultCoachingClaimStore {
    override fun find(code: String): DefaultCoachingClaim? {
        val hit = (cache.get(code) as DefaultCoachingClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCoachingClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCoachingClaim> = inner.list()
}

fun newCachedDefaultCoachingClaimStore(inner: DefaultCoachingClaimStore, ttl: Int, clock: Clock): DefaultCoachingClaimStore {
    return CachedDefaultCoachingClaimStore(
        inner = inner,
        cache = newTtlCache<DefaultCoachingClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default coaching claims. Empty fields match all. */
data class DefaultCoachingClaimQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCoachingClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DefaultCoachingClaim>): MutableList<DefaultCoachingClaim> {
        val matched = filter(items) { candidate: DefaultCoachingClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCoachingClaims(store: DefaultCoachingClaimStore, query: DefaultCoachingClaimQuery): MutableList<DefaultCoachingClaim> {
    return query.apply(store.list())
}
