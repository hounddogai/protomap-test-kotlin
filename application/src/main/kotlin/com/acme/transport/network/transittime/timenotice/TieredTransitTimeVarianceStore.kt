package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered transit time variances by code. */
interface TieredTransitTimeVarianceStore {
    fun find(code: String): TieredTransitTimeVariance?
    fun save(item: TieredTransitTimeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTransitTimeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTransitTimeVarianceStore(
    var items: MutableMap<String, TieredTransitTimeVariance>,
    var order: MutableList<String>,
) : TieredTransitTimeVarianceStore {
    override fun find(code: String): TieredTransitTimeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTransitTimeVariance) {
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

    override fun list(): MutableList<TieredTransitTimeVariance> {
        val out = mutableListOf<TieredTransitTimeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTransitTimeVarianceStore(): TieredTransitTimeVarianceStore {
    return MemoryTieredTransitTimeVarianceStore(
        mutableMapOf<String, TieredTransitTimeVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTransitTimeVarianceStore(
    var inner: TieredTransitTimeVarianceStore,
    var cache: TtlCache<TieredTransitTimeVariance>,
    var hits: Int,
    var misses: Int,
) : TieredTransitTimeVarianceStore {
    override fun find(code: String): TieredTransitTimeVariance? {
        val hit = (cache.get(code) as TieredTransitTimeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTransitTimeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTransitTimeVariance> = inner.list()
}

fun newCachedTieredTransitTimeVarianceStore(inner: TieredTransitTimeVarianceStore, ttl: Int, clock: Clock): TieredTransitTimeVarianceStore {
    return CachedTieredTransitTimeVarianceStore(
        inner = inner,
        cache = newTtlCache<TieredTransitTimeVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered transit time variances. Empty fields match all. */
data class TieredTransitTimeVarianceQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTransitTimeVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTransitTimeVariance>): MutableList<TieredTransitTimeVariance> {
        val matched = filter(items) { candidate: TieredTransitTimeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTransitTimeVariances(store: TieredTransitTimeVarianceStore, query: TieredTransitTimeVarianceQuery): MutableList<TieredTransitTimeVariance> {
    return query.apply(store.list())
}
