package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default coaching metrics by code. */
interface DefaultCoachingMetricStore {
    fun find(code: String): DefaultCoachingMetric?
    fun save(item: DefaultCoachingMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCoachingMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCoachingMetricStore(
    var items: MutableMap<String, DefaultCoachingMetric>,
    var order: MutableList<String>,
) : DefaultCoachingMetricStore {
    override fun find(code: String): DefaultCoachingMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCoachingMetric) {
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

    override fun list(): MutableList<DefaultCoachingMetric> {
        val out = mutableListOf<DefaultCoachingMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCoachingMetricStore(): DefaultCoachingMetricStore {
    return MemoryDefaultCoachingMetricStore(
        mutableMapOf<String, DefaultCoachingMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCoachingMetricStore(
    var inner: DefaultCoachingMetricStore,
    var cache: TtlCache<DefaultCoachingMetric>,
    var hits: Int,
    var misses: Int,
) : DefaultCoachingMetricStore {
    override fun find(code: String): DefaultCoachingMetric? {
        val hit = (cache.get(code) as DefaultCoachingMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCoachingMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCoachingMetric> = inner.list()
}

fun newCachedDefaultCoachingMetricStore(inner: DefaultCoachingMetricStore, ttl: Int, clock: Clock): DefaultCoachingMetricStore {
    return CachedDefaultCoachingMetricStore(
        inner = inner,
        cache = newTtlCache<DefaultCoachingMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default coaching metrics. Empty fields match all. */
data class DefaultCoachingMetricQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCoachingMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<DefaultCoachingMetric>): MutableList<DefaultCoachingMetric> {
        val matched = filter(items) { candidate: DefaultCoachingMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCoachingMetrics(store: DefaultCoachingMetricStore, query: DefaultCoachingMetricQuery): MutableList<DefaultCoachingMetric> {
    return query.apply(store.list())
}
