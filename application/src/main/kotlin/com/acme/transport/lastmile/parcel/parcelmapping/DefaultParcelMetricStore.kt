package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel metrics by code. */
interface DefaultParcelMetricStore {
    fun find(code: String): DefaultParcelMetric?
    fun save(item: DefaultParcelMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelMetricStore(
    var items: MutableMap<String, DefaultParcelMetric>,
    var order: MutableList<String>,
) : DefaultParcelMetricStore {
    override fun find(code: String): DefaultParcelMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelMetric) {
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

    override fun list(): MutableList<DefaultParcelMetric> {
        val out = mutableListOf<DefaultParcelMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelMetricStore(): DefaultParcelMetricStore {
    return MemoryDefaultParcelMetricStore(mutableMapOf<String, DefaultParcelMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelMetricStore(
    var inner: DefaultParcelMetricStore,
    var cache: TtlCache<DefaultParcelMetric>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelMetricStore {
    override fun find(code: String): DefaultParcelMetric? {
        val hit = (cache.get(code) as DefaultParcelMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelMetric> = inner.list()
}

fun newCachedDefaultParcelMetricStore(inner: DefaultParcelMetricStore, ttl: Int, clock: Clock): DefaultParcelMetricStore {
    return CachedDefaultParcelMetricStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel metrics. Empty fields match all. */
data class DefaultParcelMetricQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelMetric): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultParcelMetric>): MutableList<DefaultParcelMetric> {
        val matched = filter(items) { candidate: DefaultParcelMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelMetrics(store: DefaultParcelMetricStore, query: DefaultParcelMetricQuery): MutableList<DefaultParcelMetric> {
    return query.apply(store.list())
}
