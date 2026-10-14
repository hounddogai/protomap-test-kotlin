package com.acme.transport.customs.declaration.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted declaration claims by code. */
interface WeightedDeclarationClaimStore {
    fun find(code: String): WeightedDeclarationClaim?
    fun save(item: WeightedDeclarationClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDeclarationClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDeclarationClaimStore(
    var items: MutableMap<String, WeightedDeclarationClaim>,
    var order: MutableList<String>,
) : WeightedDeclarationClaimStore {
    override fun find(code: String): WeightedDeclarationClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDeclarationClaim) {
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

    override fun list(): MutableList<WeightedDeclarationClaim> {
        val out = mutableListOf<WeightedDeclarationClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDeclarationClaimStore(): WeightedDeclarationClaimStore {
    return MemoryWeightedDeclarationClaimStore(
        mutableMapOf<String, WeightedDeclarationClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDeclarationClaimStore(
    var inner: WeightedDeclarationClaimStore,
    var cache: TtlCache<WeightedDeclarationClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedDeclarationClaimStore {
    override fun find(code: String): WeightedDeclarationClaim? {
        val hit = (cache.get(code) as WeightedDeclarationClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDeclarationClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDeclarationClaim> = inner.list()
}

fun newCachedWeightedDeclarationClaimStore(inner: WeightedDeclarationClaimStore, ttl: Int, clock: Clock): WeightedDeclarationClaimStore {
    return CachedWeightedDeclarationClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedDeclarationClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted declaration claims. Empty fields match all. */
data class WeightedDeclarationClaimQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDeclarationClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDeclarationClaim>): MutableList<WeightedDeclarationClaim> {
        val matched = filter(items) { candidate: WeightedDeclarationClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDeclarationClaims(store: WeightedDeclarationClaimStore, query: WeightedDeclarationClaimQuery): MutableList<WeightedDeclarationClaim> {
    return query.apply(store.list())
}
