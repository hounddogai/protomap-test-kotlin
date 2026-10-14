package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps default trailer metrics by code. */
interface DefaultTrailerMetricStore {
    fun find(code: String): DefaultTrailerMetric?
    fun save(item: DefaultTrailerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTrailerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTrailerMetricStore(
    var items: MutableMap<String, DefaultTrailerMetric>,
    var order: MutableList<String>,
) : DefaultTrailerMetricStore {
    override fun find(code: String): DefaultTrailerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTrailerMetric) {
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

    override fun list(): MutableList<DefaultTrailerMetric> {
        val out = mutableListOf<DefaultTrailerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTrailerMetricStore(): DefaultTrailerMetricStore {
    return MemoryDefaultTrailerMetricStore(
        mutableMapOf<String, DefaultTrailerMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTrailerMetricStore(
    var inner: DefaultTrailerMetricStore,
    var cache: TtlCache<DefaultTrailerMetric>,
    var hits: Int,
    var misses: Int,
) : DefaultTrailerMetricStore {
    override fun find(code: String): DefaultTrailerMetric? {
        val hit = (cache.get(code) as DefaultTrailerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTrailerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTrailerMetric> = inner.list()
}

fun newCachedDefaultTrailerMetricStore(inner: DefaultTrailerMetricStore, ttl: Int, clock: Clock): DefaultTrailerMetricStore {
    return CachedDefaultTrailerMetricStore(
        inner = inner,
        cache = newTtlCache<DefaultTrailerMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default trailer metrics. Empty fields match all. */
data class DefaultTrailerMetricQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTrailerMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTrailerMetric>): MutableList<DefaultTrailerMetric> {
        val matched = filter(items) { candidate: DefaultTrailerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTrailerMetrics(store: DefaultTrailerMetricStore, query: DefaultTrailerMetricQuery): MutableList<DefaultTrailerMetric> {
    return query.apply(store.list())
}
