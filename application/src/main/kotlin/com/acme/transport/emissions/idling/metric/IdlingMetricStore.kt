package com.acme.transport.emissions.idling.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling metrics by code. */
interface IdlingMetricStore {
    fun find(code: String): IdlingMetric?
    fun save(item: IdlingMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingMetricStore(
    var items: MutableMap<String, IdlingMetric>,
    var order: MutableList<String>,
) : IdlingMetricStore {
    override fun find(code: String): IdlingMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingMetric) {
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

    override fun list(): MutableList<IdlingMetric> {
        val out = mutableListOf<IdlingMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingMetricStore(): IdlingMetricStore {
    return MemoryIdlingMetricStore(mutableMapOf<String, IdlingMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingMetricStore(
    var inner: IdlingMetricStore,
    var cache: TtlCache<IdlingMetric>,
    var hits: Int,
    var misses: Int,
) : IdlingMetricStore {
    override fun find(code: String): IdlingMetric? {
        val hit = (cache.get(code) as IdlingMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingMetric> = inner.list()
}

fun newCachedIdlingMetricStore(inner: IdlingMetricStore, ttl: Int, clock: Clock): IdlingMetricStore {
    return CachedIdlingMetricStore(
        inner = inner,
        cache = newTtlCache<IdlingMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling metrics. Empty fields match all. */
data class IdlingMetricQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingMetric>): MutableList<IdlingMetric> {
        val matched = filter(items) { candidate: IdlingMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingMetrics(store: IdlingMetricStore, query: IdlingMetricQuery): MutableList<IdlingMetric> {
    return query.apply(store.list())
}
