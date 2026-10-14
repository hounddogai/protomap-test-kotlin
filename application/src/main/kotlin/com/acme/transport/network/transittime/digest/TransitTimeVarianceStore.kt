package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time variances by code. */
interface TransitTimeVarianceStore {
    fun find(code: String): TransitTimeVariance?
    fun save(item: TransitTimeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeVarianceStore(
    var items: MutableMap<String, TransitTimeVariance>,
    var order: MutableList<String>,
) : TransitTimeVarianceStore {
    override fun find(code: String): TransitTimeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeVariance) {
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

    override fun list(): MutableList<TransitTimeVariance> {
        val out = mutableListOf<TransitTimeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeVarianceStore(): TransitTimeVarianceStore {
    return MemoryTransitTimeVarianceStore(mutableMapOf<String, TransitTimeVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeVarianceStore(
    var inner: TransitTimeVarianceStore,
    var cache: TtlCache<TransitTimeVariance>,
    var hits: Int,
    var misses: Int,
) : TransitTimeVarianceStore {
    override fun find(code: String): TransitTimeVariance? {
        val hit = (cache.get(code) as TransitTimeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeVariance> = inner.list()
}

fun newCachedTransitTimeVarianceStore(inner: TransitTimeVarianceStore, ttl: Int, clock: Clock): TransitTimeVarianceStore {
    return CachedTransitTimeVarianceStore(
        inner = inner,
        cache = newTtlCache<TransitTimeVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time variances. Empty fields match all. */
data class TransitTimeVarianceQuery(
    var unit: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeVariance>): MutableList<TransitTimeVariance> {
        val matched = filter(items) { candidate: TransitTimeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeVariances(store: TransitTimeVarianceStore, query: TransitTimeVarianceQuery): MutableList<TransitTimeVariance> {
    return query.apply(store.list())
}
