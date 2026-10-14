package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan metrics by code. */
interface LanePlanMetricStore {
    fun find(code: String): LanePlanMetric?
    fun save(item: LanePlanMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanMetricStore(
    var items: MutableMap<String, LanePlanMetric>,
    var order: MutableList<String>,
) : LanePlanMetricStore {
    override fun find(code: String): LanePlanMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanMetric) {
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

    override fun list(): MutableList<LanePlanMetric> {
        val out = mutableListOf<LanePlanMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanMetricStore(): LanePlanMetricStore {
    return MemoryLanePlanMetricStore(mutableMapOf<String, LanePlanMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanMetricStore(
    var inner: LanePlanMetricStore,
    var cache: TtlCache<LanePlanMetric>,
    var hits: Int,
    var misses: Int,
) : LanePlanMetricStore {
    override fun find(code: String): LanePlanMetric? {
        val hit = (cache.get(code) as LanePlanMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanMetric> = inner.list()
}

fun newCachedLanePlanMetricStore(inner: LanePlanMetricStore, ttl: Int, clock: Clock): LanePlanMetricStore {
    return CachedLanePlanMetricStore(
        inner = inner,
        cache = newTtlCache<LanePlanMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan metrics. Empty fields match all. */
data class LanePlanMetricQuery(
    var lane: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanMetric>): MutableList<LanePlanMetric> {
        val matched = filter(items) { candidate: LanePlanMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanMetrics(store: LanePlanMetricStore, query: LanePlanMetricQuery): MutableList<LanePlanMetric> {
    return query.apply(store.list())
}
