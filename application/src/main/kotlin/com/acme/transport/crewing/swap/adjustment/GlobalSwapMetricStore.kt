package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global swap metrics by code. */
interface GlobalSwapMetricStore {
    fun find(code: String): GlobalSwapMetric?
    fun save(item: GlobalSwapMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalSwapMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalSwapMetricStore(
    var items: MutableMap<String, GlobalSwapMetric>,
    var order: MutableList<String>,
) : GlobalSwapMetricStore {
    override fun find(code: String): GlobalSwapMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalSwapMetric) {
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

    override fun list(): MutableList<GlobalSwapMetric> {
        val out = mutableListOf<GlobalSwapMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalSwapMetricStore(): GlobalSwapMetricStore {
    return MemoryGlobalSwapMetricStore(mutableMapOf<String, GlobalSwapMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalSwapMetricStore(
    var inner: GlobalSwapMetricStore,
    var cache: TtlCache<GlobalSwapMetric>,
    var hits: Int,
    var misses: Int,
) : GlobalSwapMetricStore {
    override fun find(code: String): GlobalSwapMetric? {
        val hit = (cache.get(code) as GlobalSwapMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalSwapMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalSwapMetric> = inner.list()
}

fun newCachedGlobalSwapMetricStore(inner: GlobalSwapMetricStore, ttl: Int, clock: Clock): GlobalSwapMetricStore {
    return CachedGlobalSwapMetricStore(
        inner = inner,
        cache = newTtlCache<GlobalSwapMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global swap metrics. Empty fields match all. */
data class GlobalSwapMetricQuery(
    var lane: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalSwapMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<GlobalSwapMetric>): MutableList<GlobalSwapMetric> {
        val matched = filter(items) { candidate: GlobalSwapMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalSwapMetrics(store: GlobalSwapMetricStore, query: GlobalSwapMetricQuery): MutableList<GlobalSwapMetric> {
    return query.apply(store.list())
}
