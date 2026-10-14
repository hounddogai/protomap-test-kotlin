package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scorecard metrics by code. */
interface ScorecardMetricStore {
    fun find(code: String): ScorecardMetric?
    fun save(item: ScorecardMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScorecardMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScorecardMetricStore(
    var items: MutableMap<String, ScorecardMetric>,
    var order: MutableList<String>,
) : ScorecardMetricStore {
    override fun find(code: String): ScorecardMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScorecardMetric) {
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

    override fun list(): MutableList<ScorecardMetric> {
        val out = mutableListOf<ScorecardMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScorecardMetricStore(): ScorecardMetricStore {
    return MemoryScorecardMetricStore(mutableMapOf<String, ScorecardMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScorecardMetricStore(
    var inner: ScorecardMetricStore,
    var cache: TtlCache<ScorecardMetric>,
    var hits: Int,
    var misses: Int,
) : ScorecardMetricStore {
    override fun find(code: String): ScorecardMetric? {
        val hit = (cache.get(code) as ScorecardMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScorecardMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScorecardMetric> = inner.list()
}

fun newCachedScorecardMetricStore(inner: ScorecardMetricStore, ttl: Int, clock: Clock): ScorecardMetricStore {
    return CachedScorecardMetricStore(
        inner = inner,
        cache = newTtlCache<ScorecardMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scorecard metrics. Empty fields match all. */
data class ScorecardMetricQuery(
    var unit: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScorecardMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScorecardMetric>): MutableList<ScorecardMetric> {
        val matched = filter(items) { candidate: ScorecardMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScorecardMetrics(store: ScorecardMetricStore, query: ScorecardMetricQuery): MutableList<ScorecardMetric> {
    return query.apply(store.list())
}
