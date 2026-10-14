package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation metrics by code. */
interface ViolationMetricStore {
    fun find(code: String): ViolationMetric?
    fun save(item: ViolationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationMetricStore(
    var items: MutableMap<String, ViolationMetric>,
    var order: MutableList<String>,
) : ViolationMetricStore {
    override fun find(code: String): ViolationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationMetric) {
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

    override fun list(): MutableList<ViolationMetric> {
        val out = mutableListOf<ViolationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationMetricStore(): ViolationMetricStore {
    return MemoryViolationMetricStore(mutableMapOf<String, ViolationMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationMetricStore(
    var inner: ViolationMetricStore,
    var cache: TtlCache<ViolationMetric>,
    var hits: Int,
    var misses: Int,
) : ViolationMetricStore {
    override fun find(code: String): ViolationMetric? {
        val hit = (cache.get(code) as ViolationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationMetric> = inner.list()
}

fun newCachedViolationMetricStore(inner: ViolationMetricStore, ttl: Int, clock: Clock): ViolationMetricStore {
    return CachedViolationMetricStore(
        inner = inner,
        cache = newTtlCache<ViolationMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation metrics. Empty fields match all. */
data class ViolationMetricQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ViolationMetric>): MutableList<ViolationMetric> {
        val matched = filter(items) { candidate: ViolationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationMetrics(store: ViolationMetricStore, query: ViolationMetricQuery): MutableList<ViolationMetric> {
    return query.apply(store.list())
}
