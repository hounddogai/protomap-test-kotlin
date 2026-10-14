package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback accessorial variances by code. */
interface FallbackAccessorialVarianceStore {
    fun find(code: String): FallbackAccessorialVariance?
    fun save(item: FallbackAccessorialVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAccessorialVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAccessorialVarianceStore(
    var items: MutableMap<String, FallbackAccessorialVariance>,
    var order: MutableList<String>,
) : FallbackAccessorialVarianceStore {
    override fun find(code: String): FallbackAccessorialVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAccessorialVariance) {
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

    override fun list(): MutableList<FallbackAccessorialVariance> {
        val out = mutableListOf<FallbackAccessorialVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAccessorialVarianceStore(): FallbackAccessorialVarianceStore {
    return MemoryFallbackAccessorialVarianceStore(
        mutableMapOf<String, FallbackAccessorialVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAccessorialVarianceStore(
    var inner: FallbackAccessorialVarianceStore,
    var cache: TtlCache<FallbackAccessorialVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackAccessorialVarianceStore {
    override fun find(code: String): FallbackAccessorialVariance? {
        val hit = (cache.get(code) as FallbackAccessorialVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAccessorialVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAccessorialVariance> = inner.list()
}

fun newCachedFallbackAccessorialVarianceStore(inner: FallbackAccessorialVarianceStore, ttl: Int, clock: Clock): FallbackAccessorialVarianceStore {
    return CachedFallbackAccessorialVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackAccessorialVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback accessorial variances. Empty fields match all. */
data class FallbackAccessorialVarianceQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAccessorialVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<FallbackAccessorialVariance>): MutableList<FallbackAccessorialVariance> {
        val matched = filter(items) { candidate: FallbackAccessorialVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAccessorialVariances(store: FallbackAccessorialVarianceStore, query: FallbackAccessorialVarianceQuery): MutableList<FallbackAccessorialVariance> {
    return query.apply(store.list())
}
