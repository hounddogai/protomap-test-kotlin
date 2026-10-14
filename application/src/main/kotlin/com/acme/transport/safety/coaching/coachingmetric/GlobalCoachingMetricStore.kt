package com.acme.transport.safety.coaching.coachingmetric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global coaching metrics by code. */
interface GlobalCoachingMetricStore {
    fun find(code: String): GlobalCoachingMetric?
    fun save(item: GlobalCoachingMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCoachingMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCoachingMetricStore(
    var items: MutableMap<String, GlobalCoachingMetric>,
    var order: MutableList<String>,
) : GlobalCoachingMetricStore {
    override fun find(code: String): GlobalCoachingMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCoachingMetric) {
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

    override fun list(): MutableList<GlobalCoachingMetric> {
        val out = mutableListOf<GlobalCoachingMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCoachingMetricStore(): GlobalCoachingMetricStore {
    return MemoryGlobalCoachingMetricStore(
        mutableMapOf<String, GlobalCoachingMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCoachingMetricStore(
    var inner: GlobalCoachingMetricStore,
    var cache: TtlCache<GlobalCoachingMetric>,
    var hits: Int,
    var misses: Int,
) : GlobalCoachingMetricStore {
    override fun find(code: String): GlobalCoachingMetric? {
        val hit = (cache.get(code) as GlobalCoachingMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCoachingMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCoachingMetric> = inner.list()
}

fun newCachedGlobalCoachingMetricStore(inner: GlobalCoachingMetricStore, ttl: Int, clock: Clock): GlobalCoachingMetricStore {
    return CachedGlobalCoachingMetricStore(
        inner = inner,
        cache = newTtlCache<GlobalCoachingMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global coaching metrics. Empty fields match all. */
data class GlobalCoachingMetricQuery(
    var depot: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCoachingMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<GlobalCoachingMetric>): MutableList<GlobalCoachingMetric> {
        val matched = filter(items) { candidate: GlobalCoachingMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCoachingMetrics(store: GlobalCoachingMetricStore, query: GlobalCoachingMetricQuery): MutableList<GlobalCoachingMetric> {
    return query.apply(store.list())
}
