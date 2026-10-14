package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold metrics by code. */
interface HoldMetricStore {
    fun find(code: String): HoldMetric?
    fun save(item: HoldMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldMetricStore(
    var items: MutableMap<String, HoldMetric>,
    var order: MutableList<String>,
) : HoldMetricStore {
    override fun find(code: String): HoldMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldMetric) {
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

    override fun list(): MutableList<HoldMetric> {
        val out = mutableListOf<HoldMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldMetricStore(): HoldMetricStore {
    return MemoryHoldMetricStore(mutableMapOf<String, HoldMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldMetricStore(
    var inner: HoldMetricStore,
    var cache: TtlCache<HoldMetric>,
    var hits: Int,
    var misses: Int,
) : HoldMetricStore {
    override fun find(code: String): HoldMetric? {
        val hit = (cache.get(code) as HoldMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldMetric> = inner.list()
}

fun newCachedHoldMetricStore(inner: HoldMetricStore, ttl: Int, clock: Clock): HoldMetricStore {
    return CachedHoldMetricStore(
        inner = inner,
        cache = newTtlCache<HoldMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold metrics. Empty fields match all. */
data class HoldMetricQuery(
    var category: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldMetric): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldMetric>): MutableList<HoldMetric> {
        val matched = filter(items) { candidate: HoldMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldMetrics(store: HoldMetricStore, query: HoldMetricQuery): MutableList<HoldMetric> {
    return query.apply(store.list())
}
