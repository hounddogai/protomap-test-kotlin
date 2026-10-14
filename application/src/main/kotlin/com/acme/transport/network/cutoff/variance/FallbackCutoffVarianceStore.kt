package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback cutoff variances by code. */
interface FallbackCutoffVarianceStore {
    fun find(code: String): FallbackCutoffVariance?
    fun save(item: FallbackCutoffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCutoffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCutoffVarianceStore(
    var items: MutableMap<String, FallbackCutoffVariance>,
    var order: MutableList<String>,
) : FallbackCutoffVarianceStore {
    override fun find(code: String): FallbackCutoffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCutoffVariance) {
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

    override fun list(): MutableList<FallbackCutoffVariance> {
        val out = mutableListOf<FallbackCutoffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCutoffVarianceStore(): FallbackCutoffVarianceStore {
    return MemoryFallbackCutoffVarianceStore(
        mutableMapOf<String, FallbackCutoffVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCutoffVarianceStore(
    var inner: FallbackCutoffVarianceStore,
    var cache: TtlCache<FallbackCutoffVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackCutoffVarianceStore {
    override fun find(code: String): FallbackCutoffVariance? {
        val hit = (cache.get(code) as FallbackCutoffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCutoffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCutoffVariance> = inner.list()
}

fun newCachedFallbackCutoffVarianceStore(inner: FallbackCutoffVarianceStore, ttl: Int, clock: Clock): FallbackCutoffVarianceStore {
    return CachedFallbackCutoffVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackCutoffVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback cutoff variances. Empty fields match all. */
data class FallbackCutoffVarianceQuery(
    var depot: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCutoffVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCutoffVariance>): MutableList<FallbackCutoffVariance> {
        val matched = filter(items) { candidate: FallbackCutoffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCutoffVariances(store: FallbackCutoffVarianceStore, query: FallbackCutoffVarianceQuery): MutableList<FallbackCutoffVariance> {
    return query.apply(store.list())
}
