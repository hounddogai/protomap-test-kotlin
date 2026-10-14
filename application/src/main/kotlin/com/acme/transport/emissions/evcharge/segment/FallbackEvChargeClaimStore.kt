package com.acme.transport.emissions.evcharge.segment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback ev charge claims by code. */
interface FallbackEvChargeClaimStore {
    fun find(code: String): FallbackEvChargeClaim?
    fun save(item: FallbackEvChargeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEvChargeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEvChargeClaimStore(
    var items: MutableMap<String, FallbackEvChargeClaim>,
    var order: MutableList<String>,
) : FallbackEvChargeClaimStore {
    override fun find(code: String): FallbackEvChargeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEvChargeClaim) {
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

    override fun list(): MutableList<FallbackEvChargeClaim> {
        val out = mutableListOf<FallbackEvChargeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEvChargeClaimStore(): FallbackEvChargeClaimStore {
    return MemoryFallbackEvChargeClaimStore(
        mutableMapOf<String, FallbackEvChargeClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEvChargeClaimStore(
    var inner: FallbackEvChargeClaimStore,
    var cache: TtlCache<FallbackEvChargeClaim>,
    var hits: Int,
    var misses: Int,
) : FallbackEvChargeClaimStore {
    override fun find(code: String): FallbackEvChargeClaim? {
        val hit = (cache.get(code) as FallbackEvChargeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEvChargeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEvChargeClaim> = inner.list()
}

fun newCachedFallbackEvChargeClaimStore(inner: FallbackEvChargeClaimStore, ttl: Int, clock: Clock): FallbackEvChargeClaimStore {
    return CachedFallbackEvChargeClaimStore(
        inner = inner,
        cache = newTtlCache<FallbackEvChargeClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback ev charge claims. Empty fields match all. */
data class FallbackEvChargeClaimQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEvChargeClaim): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEvChargeClaim>): MutableList<FallbackEvChargeClaim> {
        val matched = filter(items) { candidate: FallbackEvChargeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEvChargeClaims(store: FallbackEvChargeClaimStore, query: FallbackEvChargeClaimQuery): MutableList<FallbackEvChargeClaim> {
    return query.apply(store.list())
}
