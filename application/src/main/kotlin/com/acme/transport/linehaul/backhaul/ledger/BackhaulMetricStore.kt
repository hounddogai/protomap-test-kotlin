package com.acme.transport.linehaul.backhaul.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul metrics by code. */
interface BackhaulMetricStore {
    fun find(code: String): BackhaulMetric?
    fun save(item: BackhaulMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulMetricStore(
    var items: MutableMap<String, BackhaulMetric>,
    var order: MutableList<String>,
) : BackhaulMetricStore {
    override fun find(code: String): BackhaulMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulMetric) {
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

    override fun list(): MutableList<BackhaulMetric> {
        val out = mutableListOf<BackhaulMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulMetricStore(): BackhaulMetricStore {
    return MemoryBackhaulMetricStore(mutableMapOf<String, BackhaulMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulMetricStore(
    var inner: BackhaulMetricStore,
    var cache: TtlCache<BackhaulMetric>,
    var hits: Int,
    var misses: Int,
) : BackhaulMetricStore {
    override fun find(code: String): BackhaulMetric? {
        val hit = (cache.get(code) as BackhaulMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulMetric> = inner.list()
}

fun newCachedBackhaulMetricStore(inner: BackhaulMetricStore, ttl: Int, clock: Clock): BackhaulMetricStore {
    return CachedBackhaulMetricStore(
        inner = inner,
        cache = newTtlCache<BackhaulMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul metrics. Empty fields match all. */
data class BackhaulMetricQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulMetric>): MutableList<BackhaulMetric> {
        val matched = filter(items) { candidate: BackhaulMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulMetrics(store: BackhaulMetricStore, query: BackhaulMetricQuery): MutableList<BackhaulMetric> {
    return query.apply(store.list())
}
