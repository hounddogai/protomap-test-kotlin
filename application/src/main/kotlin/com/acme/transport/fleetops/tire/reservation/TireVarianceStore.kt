package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire variances by code. */
interface TireVarianceStore {
    fun find(code: String): TireVariance?
    fun save(item: TireVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireVarianceStore(
    var items: MutableMap<String, TireVariance>,
    var order: MutableList<String>,
) : TireVarianceStore {
    override fun find(code: String): TireVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireVariance) {
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

    override fun list(): MutableList<TireVariance> {
        val out = mutableListOf<TireVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireVarianceStore(): TireVarianceStore {
    return MemoryTireVarianceStore(mutableMapOf<String, TireVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireVarianceStore(
    var inner: TireVarianceStore,
    var cache: TtlCache<TireVariance>,
    var hits: Int,
    var misses: Int,
) : TireVarianceStore {
    override fun find(code: String): TireVariance? {
        val hit = (cache.get(code) as TireVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireVariance> = inner.list()
}

fun newCachedTireVarianceStore(inner: TireVarianceStore, ttl: Int, clock: Clock): TireVarianceStore {
    return CachedTireVarianceStore(
        inner = inner,
        cache = newTtlCache<TireVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire variances. Empty fields match all. */
data class TireVarianceQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireVariance>): MutableList<TireVariance> {
        val matched = filter(items) { candidate: TireVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireVariances(store: TireVarianceStore, query: TireVarianceQuery): MutableList<TireVariance> {
    return query.apply(store.list())
}
