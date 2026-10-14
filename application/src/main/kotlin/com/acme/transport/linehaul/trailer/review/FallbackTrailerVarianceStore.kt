package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback trailer variances by code. */
interface FallbackTrailerVarianceStore {
    fun find(code: String): FallbackTrailerVariance?
    fun save(item: FallbackTrailerVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTrailerVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTrailerVarianceStore(
    var items: MutableMap<String, FallbackTrailerVariance>,
    var order: MutableList<String>,
) : FallbackTrailerVarianceStore {
    override fun find(code: String): FallbackTrailerVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTrailerVariance) {
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

    override fun list(): MutableList<FallbackTrailerVariance> {
        val out = mutableListOf<FallbackTrailerVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTrailerVarianceStore(): FallbackTrailerVarianceStore {
    return MemoryFallbackTrailerVarianceStore(
        mutableMapOf<String, FallbackTrailerVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTrailerVarianceStore(
    var inner: FallbackTrailerVarianceStore,
    var cache: TtlCache<FallbackTrailerVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackTrailerVarianceStore {
    override fun find(code: String): FallbackTrailerVariance? {
        val hit = (cache.get(code) as FallbackTrailerVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTrailerVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTrailerVariance> = inner.list()
}

fun newCachedFallbackTrailerVarianceStore(inner: FallbackTrailerVarianceStore, ttl: Int, clock: Clock): FallbackTrailerVarianceStore {
    return CachedFallbackTrailerVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackTrailerVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback trailer variances. Empty fields match all. */
data class FallbackTrailerVarianceQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTrailerVariance): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<FallbackTrailerVariance>): MutableList<FallbackTrailerVariance> {
        val matched = filter(items) { candidate: FallbackTrailerVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTrailerVariances(store: FallbackTrailerVarianceStore, query: FallbackTrailerVarianceQuery): MutableList<FallbackTrailerVariance> {
    return query.apply(store.list())
}
