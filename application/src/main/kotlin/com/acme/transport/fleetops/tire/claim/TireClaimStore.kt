package com.acme.transport.fleetops.tire.claim

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire claims by code. */
interface TireClaimStore {
    fun find(code: String): TireClaim?
    fun save(item: TireClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireClaimStore(
    var items: MutableMap<String, TireClaim>,
    var order: MutableList<String>,
) : TireClaimStore {
    override fun find(code: String): TireClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireClaim) {
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

    override fun list(): MutableList<TireClaim> {
        val out = mutableListOf<TireClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireClaimStore(): TireClaimStore {
    return MemoryTireClaimStore(mutableMapOf<String, TireClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireClaimStore(
    var inner: TireClaimStore,
    var cache: TtlCache<TireClaim>,
    var hits: Int,
    var misses: Int,
) : TireClaimStore {
    override fun find(code: String): TireClaim? {
        val hit = (cache.get(code) as TireClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireClaim> = inner.list()
}

fun newCachedTireClaimStore(inner: TireClaimStore, ttl: Int, clock: Clock): TireClaimStore {
    return CachedTireClaimStore(
        inner = inner,
        cache = newTtlCache<TireClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire claims. Empty fields match all. */
data class TireClaimQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireClaim): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<TireClaim>): MutableList<TireClaim> {
        val matched = filter(items) { candidate: TireClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireClaims(store: TireClaimStore, query: TireClaimQuery): MutableList<TireClaim> {
    return query.apply(store.list())
}
