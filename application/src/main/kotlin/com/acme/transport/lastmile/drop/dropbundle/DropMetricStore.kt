package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop metrics by code. */
interface DropMetricStore {
    fun find(code: String): DropMetric?
    fun save(item: DropMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropMetricStore(
    var items: MutableMap<String, DropMetric>,
    var order: MutableList<String>,
) : DropMetricStore {
    override fun find(code: String): DropMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropMetric) {
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

    override fun list(): MutableList<DropMetric> {
        val out = mutableListOf<DropMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropMetricStore(): DropMetricStore {
    return MemoryDropMetricStore(mutableMapOf<String, DropMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropMetricStore(
    var inner: DropMetricStore,
    var cache: TtlCache<DropMetric>,
    var hits: Int,
    var misses: Int,
) : DropMetricStore {
    override fun find(code: String): DropMetric? {
        val hit = (cache.get(code) as DropMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropMetric> = inner.list()
}

fun newCachedDropMetricStore(inner: DropMetricStore, ttl: Int, clock: Clock): DropMetricStore {
    return CachedDropMetricStore(
        inner = inner,
        cache = newTtlCache<DropMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop metrics. Empty fields match all. */
data class DropMetricQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropMetric): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DropMetric>): MutableList<DropMetric> {
        val matched = filter(items) { candidate: DropMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropMetrics(store: DropMetricStore, query: DropMetricQuery): MutableList<DropMetric> {
    return query.apply(store.list())
}
