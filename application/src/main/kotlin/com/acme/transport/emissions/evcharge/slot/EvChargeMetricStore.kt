package com.acme.transport.emissions.evcharge.slot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge metrics by code. */
interface EvChargeMetricStore {
    fun find(code: String): EvChargeMetric?
    fun save(item: EvChargeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeMetricStore(
    var items: MutableMap<String, EvChargeMetric>,
    var order: MutableList<String>,
) : EvChargeMetricStore {
    override fun find(code: String): EvChargeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeMetric) {
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

    override fun list(): MutableList<EvChargeMetric> {
        val out = mutableListOf<EvChargeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeMetricStore(): EvChargeMetricStore {
    return MemoryEvChargeMetricStore(mutableMapOf<String, EvChargeMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeMetricStore(
    var inner: EvChargeMetricStore,
    var cache: TtlCache<EvChargeMetric>,
    var hits: Int,
    var misses: Int,
) : EvChargeMetricStore {
    override fun find(code: String): EvChargeMetric? {
        val hit = (cache.get(code) as EvChargeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeMetric> = inner.list()
}

fun newCachedEvChargeMetricStore(inner: EvChargeMetricStore, ttl: Int, clock: Clock): EvChargeMetricStore {
    return CachedEvChargeMetricStore(
        inner = inner,
        cache = newTtlCache<EvChargeMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge metrics. Empty fields match all. */
data class EvChargeMetricQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeMetric): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeMetric>): MutableList<EvChargeMetric> {
        val matched = filter(items) { candidate: EvChargeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeMetrics(store: EvChargeMetricStore, query: EvChargeMetricQuery): MutableList<EvChargeMetric> {
    return query.apply(store.list())
}
