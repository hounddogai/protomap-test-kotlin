package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary backhaul metrics by code. */
interface PrimaryBackhaulMetricStore {
    fun find(code: String): PrimaryBackhaulMetric?
    fun save(item: PrimaryBackhaulMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryBackhaulMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryBackhaulMetricStore(
    var items: MutableMap<String, PrimaryBackhaulMetric>,
    var order: MutableList<String>,
) : PrimaryBackhaulMetricStore {
    override fun find(code: String): PrimaryBackhaulMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryBackhaulMetric) {
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

    override fun list(): MutableList<PrimaryBackhaulMetric> {
        val out = mutableListOf<PrimaryBackhaulMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryBackhaulMetricStore(): PrimaryBackhaulMetricStore {
    return MemoryPrimaryBackhaulMetricStore(
        mutableMapOf<String, PrimaryBackhaulMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryBackhaulMetricStore(
    var inner: PrimaryBackhaulMetricStore,
    var cache: TtlCache<PrimaryBackhaulMetric>,
    var hits: Int,
    var misses: Int,
) : PrimaryBackhaulMetricStore {
    override fun find(code: String): PrimaryBackhaulMetric? {
        val hit = (cache.get(code) as PrimaryBackhaulMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryBackhaulMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryBackhaulMetric> = inner.list()
}

fun newCachedPrimaryBackhaulMetricStore(inner: PrimaryBackhaulMetricStore, ttl: Int, clock: Clock): PrimaryBackhaulMetricStore {
    return CachedPrimaryBackhaulMetricStore(
        inner = inner,
        cache = newTtlCache<PrimaryBackhaulMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary backhaul metrics. Empty fields match all. */
data class PrimaryBackhaulMetricQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryBackhaulMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryBackhaulMetric>): MutableList<PrimaryBackhaulMetric> {
        val matched = filter(items) { candidate: PrimaryBackhaulMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryBackhaulMetrics(store: PrimaryBackhaulMetricStore, query: PrimaryBackhaulMetricQuery): MutableList<PrimaryBackhaulMetric> {
    return query.apply(store.list())
}
