package com.acme.transport.emissions.evcharge.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling ev charge metrics by code. */
interface RollingEvChargeMetricStore {
    fun find(code: String): RollingEvChargeMetric?
    fun save(item: RollingEvChargeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingEvChargeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingEvChargeMetricStore(
    var items: MutableMap<String, RollingEvChargeMetric>,
    var order: MutableList<String>,
) : RollingEvChargeMetricStore {
    override fun find(code: String): RollingEvChargeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingEvChargeMetric) {
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

    override fun list(): MutableList<RollingEvChargeMetric> {
        val out = mutableListOf<RollingEvChargeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingEvChargeMetricStore(): RollingEvChargeMetricStore {
    return MemoryRollingEvChargeMetricStore(
        mutableMapOf<String, RollingEvChargeMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingEvChargeMetricStore(
    var inner: RollingEvChargeMetricStore,
    var cache: TtlCache<RollingEvChargeMetric>,
    var hits: Int,
    var misses: Int,
) : RollingEvChargeMetricStore {
    override fun find(code: String): RollingEvChargeMetric? {
        val hit = (cache.get(code) as RollingEvChargeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingEvChargeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingEvChargeMetric> = inner.list()
}

fun newCachedRollingEvChargeMetricStore(inner: RollingEvChargeMetricStore, ttl: Int, clock: Clock): RollingEvChargeMetricStore {
    return CachedRollingEvChargeMetricStore(
        inner = inner,
        cache = newTtlCache<RollingEvChargeMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling ev charge metrics. Empty fields match all. */
data class RollingEvChargeMetricQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingEvChargeMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RollingEvChargeMetric>): MutableList<RollingEvChargeMetric> {
        val matched = filter(items) { candidate: RollingEvChargeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingEvChargeMetrics(store: RollingEvChargeMetricStore, query: RollingEvChargeMetricQuery): MutableList<RollingEvChargeMetric> {
    return query.apply(store.list())
}
