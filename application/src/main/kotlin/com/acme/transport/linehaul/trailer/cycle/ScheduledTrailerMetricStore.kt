package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled trailer metrics by code. */
interface ScheduledTrailerMetricStore {
    fun find(code: String): ScheduledTrailerMetric?
    fun save(item: ScheduledTrailerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTrailerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTrailerMetricStore(
    var items: MutableMap<String, ScheduledTrailerMetric>,
    var order: MutableList<String>,
) : ScheduledTrailerMetricStore {
    override fun find(code: String): ScheduledTrailerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTrailerMetric) {
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

    override fun list(): MutableList<ScheduledTrailerMetric> {
        val out = mutableListOf<ScheduledTrailerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTrailerMetricStore(): ScheduledTrailerMetricStore {
    return MemoryScheduledTrailerMetricStore(
        mutableMapOf<String, ScheduledTrailerMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTrailerMetricStore(
    var inner: ScheduledTrailerMetricStore,
    var cache: TtlCache<ScheduledTrailerMetric>,
    var hits: Int,
    var misses: Int,
) : ScheduledTrailerMetricStore {
    override fun find(code: String): ScheduledTrailerMetric? {
        val hit = (cache.get(code) as ScheduledTrailerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTrailerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTrailerMetric> = inner.list()
}

fun newCachedScheduledTrailerMetricStore(inner: ScheduledTrailerMetricStore, ttl: Int, clock: Clock): ScheduledTrailerMetricStore {
    return CachedScheduledTrailerMetricStore(
        inner = inner,
        cache = newTtlCache<ScheduledTrailerMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled trailer metrics. Empty fields match all. */
data class ScheduledTrailerMetricQuery(
    var source: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTrailerMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTrailerMetric>): MutableList<ScheduledTrailerMetric> {
        val matched = filter(items) { candidate: ScheduledTrailerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTrailerMetrics(store: ScheduledTrailerMetricStore, query: ScheduledTrailerMetricQuery): MutableList<ScheduledTrailerMetric> {
    return query.apply(store.list())
}
