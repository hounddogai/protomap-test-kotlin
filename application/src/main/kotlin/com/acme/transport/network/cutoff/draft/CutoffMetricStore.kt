package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff metrics by code. */
interface CutoffMetricStore {
    fun find(code: String): CutoffMetric?
    fun save(item: CutoffMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffMetricStore(
    var items: MutableMap<String, CutoffMetric>,
    var order: MutableList<String>,
) : CutoffMetricStore {
    override fun find(code: String): CutoffMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffMetric) {
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

    override fun list(): MutableList<CutoffMetric> {
        val out = mutableListOf<CutoffMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffMetricStore(): CutoffMetricStore {
    return MemoryCutoffMetricStore(mutableMapOf<String, CutoffMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffMetricStore(
    var inner: CutoffMetricStore,
    var cache: TtlCache<CutoffMetric>,
    var hits: Int,
    var misses: Int,
) : CutoffMetricStore {
    override fun find(code: String): CutoffMetric? {
        val hit = (cache.get(code) as CutoffMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffMetric> = inner.list()
}

fun newCachedCutoffMetricStore(inner: CutoffMetricStore, ttl: Int, clock: Clock): CutoffMetricStore {
    return CachedCutoffMetricStore(
        inner = inner,
        cache = newTtlCache<CutoffMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff metrics. Empty fields match all. */
data class CutoffMetricQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffMetric>): MutableList<CutoffMetric> {
        val matched = filter(items) { candidate: CutoffMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffMetrics(store: CutoffMetricStore, query: CutoffMetricQuery): MutableList<CutoffMetric> {
    return query.apply(store.list())
}
