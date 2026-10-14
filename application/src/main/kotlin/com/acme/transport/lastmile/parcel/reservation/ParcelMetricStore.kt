package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel metrics by code. */
interface ParcelMetricStore {
    fun find(code: String): ParcelMetric?
    fun save(item: ParcelMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelMetricStore(
    var items: MutableMap<String, ParcelMetric>,
    var order: MutableList<String>,
) : ParcelMetricStore {
    override fun find(code: String): ParcelMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelMetric) {
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

    override fun list(): MutableList<ParcelMetric> {
        val out = mutableListOf<ParcelMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelMetricStore(): ParcelMetricStore {
    return MemoryParcelMetricStore(mutableMapOf<String, ParcelMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelMetricStore(
    var inner: ParcelMetricStore,
    var cache: TtlCache<ParcelMetric>,
    var hits: Int,
    var misses: Int,
) : ParcelMetricStore {
    override fun find(code: String): ParcelMetric? {
        val hit = (cache.get(code) as ParcelMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelMetric> = inner.list()
}

fun newCachedParcelMetricStore(inner: ParcelMetricStore, ttl: Int, clock: Clock): ParcelMetricStore {
    return CachedParcelMetricStore(
        inner = inner,
        cache = newTtlCache<ParcelMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel metrics. Empty fields match all. */
data class ParcelMetricQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelMetric>): MutableList<ParcelMetric> {
        val matched = filter(items) { candidate: ParcelMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelMetrics(store: ParcelMetricStore, query: ParcelMetricQuery): MutableList<ParcelMetric> {
    return query.apply(store.list())
}
