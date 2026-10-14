package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching metrics by code. */
interface CoachingMetricStore {
    fun find(code: String): CoachingMetric?
    fun save(item: CoachingMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingMetricStore(
    var items: MutableMap<String, CoachingMetric>,
    var order: MutableList<String>,
) : CoachingMetricStore {
    override fun find(code: String): CoachingMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingMetric) {
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

    override fun list(): MutableList<CoachingMetric> {
        val out = mutableListOf<CoachingMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingMetricStore(): CoachingMetricStore {
    return MemoryCoachingMetricStore(mutableMapOf<String, CoachingMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingMetricStore(
    var inner: CoachingMetricStore,
    var cache: TtlCache<CoachingMetric>,
    var hits: Int,
    var misses: Int,
) : CoachingMetricStore {
    override fun find(code: String): CoachingMetric? {
        val hit = (cache.get(code) as CoachingMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingMetric> = inner.list()
}

fun newCachedCoachingMetricStore(inner: CoachingMetricStore, ttl: Int, clock: Clock): CoachingMetricStore {
    return CachedCoachingMetricStore(
        inner = inner,
        cache = newTtlCache<CoachingMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching metrics. Empty fields match all. */
data class CoachingMetricQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingMetric>): MutableList<CoachingMetric> {
        val matched = filter(items) { candidate: CoachingMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingMetrics(store: CoachingMetricStore, query: CoachingMetricQuery): MutableList<CoachingMetric> {
    return query.apply(store.list())
}
