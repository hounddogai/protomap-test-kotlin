package com.acme.transport.freightpay.dispute.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback dispute rollups by code. */
interface FallbackDisputeRollupStore {
    fun find(code: String): FallbackDisputeRollup?
    fun save(item: FallbackDisputeRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDisputeRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDisputeRollupStore(
    var items: MutableMap<String, FallbackDisputeRollup>,
    var order: MutableList<String>,
) : FallbackDisputeRollupStore {
    override fun find(code: String): FallbackDisputeRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDisputeRollup) {
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

    override fun list(): MutableList<FallbackDisputeRollup> {
        val out = mutableListOf<FallbackDisputeRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDisputeRollupStore(): FallbackDisputeRollupStore {
    return MemoryFallbackDisputeRollupStore(
        mutableMapOf<String, FallbackDisputeRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDisputeRollupStore(
    var inner: FallbackDisputeRollupStore,
    var cache: TtlCache<FallbackDisputeRollup>,
    var hits: Int,
    var misses: Int,
) : FallbackDisputeRollupStore {
    override fun find(code: String): FallbackDisputeRollup? {
        val hit = (cache.get(code) as FallbackDisputeRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDisputeRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDisputeRollup> = inner.list()
}

fun newCachedFallbackDisputeRollupStore(inner: FallbackDisputeRollupStore, ttl: Int, clock: Clock): FallbackDisputeRollupStore {
    return CachedFallbackDisputeRollupStore(
        inner = inner,
        cache = newTtlCache<FallbackDisputeRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dispute rollups. Empty fields match all. */
data class FallbackDisputeRollupQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDisputeRollup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDisputeRollup>): MutableList<FallbackDisputeRollup> {
        val matched = filter(items) { candidate: FallbackDisputeRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDisputeRollups(store: FallbackDisputeRollupStore, query: FallbackDisputeRollupQuery): MutableList<FallbackDisputeRollup> {
    return query.apply(store.list())
}
