package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional dock door metrics by code. */
interface RegionalDockDoorMetricStore {
    fun find(code: String): RegionalDockDoorMetric?
    fun save(item: RegionalDockDoorMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDockDoorMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDockDoorMetricStore(
    var items: MutableMap<String, RegionalDockDoorMetric>,
    var order: MutableList<String>,
) : RegionalDockDoorMetricStore {
    override fun find(code: String): RegionalDockDoorMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDockDoorMetric) {
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

    override fun list(): MutableList<RegionalDockDoorMetric> {
        val out = mutableListOf<RegionalDockDoorMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDockDoorMetricStore(): RegionalDockDoorMetricStore {
    return MemoryRegionalDockDoorMetricStore(
        mutableMapOf<String, RegionalDockDoorMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDockDoorMetricStore(
    var inner: RegionalDockDoorMetricStore,
    var cache: TtlCache<RegionalDockDoorMetric>,
    var hits: Int,
    var misses: Int,
) : RegionalDockDoorMetricStore {
    override fun find(code: String): RegionalDockDoorMetric? {
        val hit = (cache.get(code) as RegionalDockDoorMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDockDoorMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDockDoorMetric> = inner.list()
}

fun newCachedRegionalDockDoorMetricStore(inner: RegionalDockDoorMetricStore, ttl: Int, clock: Clock): RegionalDockDoorMetricStore {
    return CachedRegionalDockDoorMetricStore(
        inner = inner,
        cache = newTtlCache<RegionalDockDoorMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional dock door metrics. Empty fields match all. */
data class RegionalDockDoorMetricQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDockDoorMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDockDoorMetric>): MutableList<RegionalDockDoorMetric> {
        val matched = filter(items) { candidate: RegionalDockDoorMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDockDoorMetrics(store: RegionalDockDoorMetricStore, query: RegionalDockDoorMetricQuery): MutableList<RegionalDockDoorMetric> {
    return query.apply(store.list())
}
