package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell metrics by code. */
interface DwellMetricStore {
    fun find(code: String): DwellMetric?
    fun save(item: DwellMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellMetricStore(
    var items: MutableMap<String, DwellMetric>,
    var order: MutableList<String>,
) : DwellMetricStore {
    override fun find(code: String): DwellMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellMetric) {
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

    override fun list(): MutableList<DwellMetric> {
        val out = mutableListOf<DwellMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellMetricStore(): DwellMetricStore {
    return MemoryDwellMetricStore(mutableMapOf<String, DwellMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellMetricStore(
    var inner: DwellMetricStore,
    var cache: TtlCache<DwellMetric>,
    var hits: Int,
    var misses: Int,
) : DwellMetricStore {
    override fun find(code: String): DwellMetric? {
        val hit = (cache.get(code) as DwellMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellMetric> = inner.list()
}

fun newCachedDwellMetricStore(inner: DwellMetricStore, ttl: Int, clock: Clock): DwellMetricStore {
    return CachedDwellMetricStore(
        inner = inner,
        cache = newTtlCache<DwellMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell metrics. Empty fields match all. */
data class DwellMetricQuery(
    var lane: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellMetric>): MutableList<DwellMetric> {
        val matched = filter(items) { candidate: DwellMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellMetrics(store: DwellMetricStore, query: DwellMetricQuery): MutableList<DwellMetric> {
    return query.apply(store.list())
}
