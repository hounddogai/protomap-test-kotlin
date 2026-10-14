package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer variances by code. */
interface TrailerVarianceStore {
    fun find(code: String): TrailerVariance?
    fun save(item: TrailerVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerVarianceStore(
    var items: MutableMap<String, TrailerVariance>,
    var order: MutableList<String>,
) : TrailerVarianceStore {
    override fun find(code: String): TrailerVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerVariance) {
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

    override fun list(): MutableList<TrailerVariance> {
        val out = mutableListOf<TrailerVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerVarianceStore(): TrailerVarianceStore {
    return MemoryTrailerVarianceStore(mutableMapOf<String, TrailerVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerVarianceStore(
    var inner: TrailerVarianceStore,
    var cache: TtlCache<TrailerVariance>,
    var hits: Int,
    var misses: Int,
) : TrailerVarianceStore {
    override fun find(code: String): TrailerVariance? {
        val hit = (cache.get(code) as TrailerVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerVariance> = inner.list()
}

fun newCachedTrailerVarianceStore(inner: TrailerVarianceStore, ttl: Int, clock: Clock): TrailerVarianceStore {
    return CachedTrailerVarianceStore(
        inner = inner,
        cache = newTtlCache<TrailerVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer variances. Empty fields match all. */
data class TrailerVarianceQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerVariance): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerVariance>): MutableList<TrailerVariance> {
        val matched = filter(items) { candidate: TrailerVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerVariances(store: TrailerVarianceStore, query: TrailerVarianceQuery): MutableList<TrailerVariance> {
    return query.apply(store.list())
}
