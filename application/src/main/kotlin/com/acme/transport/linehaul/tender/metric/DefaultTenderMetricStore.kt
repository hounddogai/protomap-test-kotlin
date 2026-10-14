package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default tender metrics by code. */
interface DefaultTenderMetricStore {
    fun find(code: String): DefaultTenderMetric?
    fun save(item: DefaultTenderMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTenderMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTenderMetricStore(
    var items: MutableMap<String, DefaultTenderMetric>,
    var order: MutableList<String>,
) : DefaultTenderMetricStore {
    override fun find(code: String): DefaultTenderMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTenderMetric) {
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

    override fun list(): MutableList<DefaultTenderMetric> {
        val out = mutableListOf<DefaultTenderMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTenderMetricStore(): DefaultTenderMetricStore {
    return MemoryDefaultTenderMetricStore(mutableMapOf<String, DefaultTenderMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTenderMetricStore(
    var inner: DefaultTenderMetricStore,
    var cache: TtlCache<DefaultTenderMetric>,
    var hits: Int,
    var misses: Int,
) : DefaultTenderMetricStore {
    override fun find(code: String): DefaultTenderMetric? {
        val hit = (cache.get(code) as DefaultTenderMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTenderMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTenderMetric> = inner.list()
}

fun newCachedDefaultTenderMetricStore(inner: DefaultTenderMetricStore, ttl: Int, clock: Clock): DefaultTenderMetricStore {
    return CachedDefaultTenderMetricStore(
        inner = inner,
        cache = newTtlCache<DefaultTenderMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tender metrics. Empty fields match all. */
data class DefaultTenderMetricQuery(
    var label: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTenderMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTenderMetric>): MutableList<DefaultTenderMetric> {
        val matched = filter(items) { candidate: DefaultTenderMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTenderMetrics(store: DefaultTenderMetricStore, query: DefaultTenderMetricQuery): MutableList<DefaultTenderMetric> {
    return query.apply(store.list())
}
