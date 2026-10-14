package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy dock door metrics by code. */
interface LegacyDockDoorMetricStore {
    fun find(code: String): LegacyDockDoorMetric?
    fun save(item: LegacyDockDoorMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDockDoorMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDockDoorMetricStore(
    var items: MutableMap<String, LegacyDockDoorMetric>,
    var order: MutableList<String>,
) : LegacyDockDoorMetricStore {
    override fun find(code: String): LegacyDockDoorMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDockDoorMetric) {
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

    override fun list(): MutableList<LegacyDockDoorMetric> {
        val out = mutableListOf<LegacyDockDoorMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDockDoorMetricStore(): LegacyDockDoorMetricStore {
    return MemoryLegacyDockDoorMetricStore(
        mutableMapOf<String, LegacyDockDoorMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDockDoorMetricStore(
    var inner: LegacyDockDoorMetricStore,
    var cache: TtlCache<LegacyDockDoorMetric>,
    var hits: Int,
    var misses: Int,
) : LegacyDockDoorMetricStore {
    override fun find(code: String): LegacyDockDoorMetric? {
        val hit = (cache.get(code) as LegacyDockDoorMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDockDoorMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDockDoorMetric> = inner.list()
}

fun newCachedLegacyDockDoorMetricStore(inner: LegacyDockDoorMetricStore, ttl: Int, clock: Clock): LegacyDockDoorMetricStore {
    return CachedLegacyDockDoorMetricStore(
        inner = inner,
        cache = newTtlCache<LegacyDockDoorMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy dock door metrics. Empty fields match all. */
data class LegacyDockDoorMetricQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDockDoorMetric): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<LegacyDockDoorMetric>): MutableList<LegacyDockDoorMetric> {
        val matched = filter(items) { candidate: LegacyDockDoorMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDockDoorMetrics(store: LegacyDockDoorMetricStore, query: LegacyDockDoorMetricQuery): MutableList<LegacyDockDoorMetric> {
    return query.apply(store.list())
}
