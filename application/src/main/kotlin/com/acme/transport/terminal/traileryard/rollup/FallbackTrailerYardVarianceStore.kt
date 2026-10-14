package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback trailer yard variances by code. */
interface FallbackTrailerYardVarianceStore {
    fun find(code: String): FallbackTrailerYardVariance?
    fun save(item: FallbackTrailerYardVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTrailerYardVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTrailerYardVarianceStore(
    var items: MutableMap<String, FallbackTrailerYardVariance>,
    var order: MutableList<String>,
) : FallbackTrailerYardVarianceStore {
    override fun find(code: String): FallbackTrailerYardVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTrailerYardVariance) {
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

    override fun list(): MutableList<FallbackTrailerYardVariance> {
        val out = mutableListOf<FallbackTrailerYardVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTrailerYardVarianceStore(): FallbackTrailerYardVarianceStore {
    return MemoryFallbackTrailerYardVarianceStore(
        mutableMapOf<String, FallbackTrailerYardVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTrailerYardVarianceStore(
    var inner: FallbackTrailerYardVarianceStore,
    var cache: TtlCache<FallbackTrailerYardVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackTrailerYardVarianceStore {
    override fun find(code: String): FallbackTrailerYardVariance? {
        val hit = (cache.get(code) as FallbackTrailerYardVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTrailerYardVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTrailerYardVariance> = inner.list()
}

fun newCachedFallbackTrailerYardVarianceStore(inner: FallbackTrailerYardVarianceStore, ttl: Int, clock: Clock): FallbackTrailerYardVarianceStore {
    return CachedFallbackTrailerYardVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackTrailerYardVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback trailer yard variances. Empty fields match all. */
data class FallbackTrailerYardVarianceQuery(
    var lane: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTrailerYardVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackTrailerYardVariance>): MutableList<FallbackTrailerYardVariance> {
        val matched = filter(items) { candidate: FallbackTrailerYardVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTrailerYardVariances(store: FallbackTrailerYardVarianceStore, query: FallbackTrailerYardVarianceQuery): MutableList<FallbackTrailerYardVariance> {
    return query.apply(store.list())
}
