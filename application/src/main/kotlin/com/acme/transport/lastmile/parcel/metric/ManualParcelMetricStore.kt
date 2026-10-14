package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual parcel metrics by code. */
interface ManualParcelMetricStore {
    fun find(code: String): ManualParcelMetric?
    fun save(item: ManualParcelMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualParcelMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualParcelMetricStore(
    var items: MutableMap<String, ManualParcelMetric>,
    var order: MutableList<String>,
) : ManualParcelMetricStore {
    override fun find(code: String): ManualParcelMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualParcelMetric) {
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

    override fun list(): MutableList<ManualParcelMetric> {
        val out = mutableListOf<ManualParcelMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualParcelMetricStore(): ManualParcelMetricStore {
    return MemoryManualParcelMetricStore(mutableMapOf<String, ManualParcelMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualParcelMetricStore(
    var inner: ManualParcelMetricStore,
    var cache: TtlCache<ManualParcelMetric>,
    var hits: Int,
    var misses: Int,
) : ManualParcelMetricStore {
    override fun find(code: String): ManualParcelMetric? {
        val hit = (cache.get(code) as ManualParcelMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualParcelMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualParcelMetric> = inner.list()
}

fun newCachedManualParcelMetricStore(inner: ManualParcelMetricStore, ttl: Int, clock: Clock): ManualParcelMetricStore {
    return CachedManualParcelMetricStore(
        inner = inner,
        cache = newTtlCache<ManualParcelMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual parcel metrics. Empty fields match all. */
data class ManualParcelMetricQuery(
    var source: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualParcelMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualParcelMetric>): MutableList<ManualParcelMetric> {
        val matched = filter(items) { candidate: ManualParcelMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualParcelMetrics(store: ManualParcelMetricStore, query: ManualParcelMetricQuery): MutableList<ManualParcelMetric> {
    return query.apply(store.list())
}
