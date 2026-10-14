package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global backhaul metrics by code. */
interface GlobalBackhaulMetricStore {
    fun find(code: String): GlobalBackhaulMetric?
    fun save(item: GlobalBackhaulMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalBackhaulMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalBackhaulMetricStore(
    var items: MutableMap<String, GlobalBackhaulMetric>,
    var order: MutableList<String>,
) : GlobalBackhaulMetricStore {
    override fun find(code: String): GlobalBackhaulMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalBackhaulMetric) {
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

    override fun list(): MutableList<GlobalBackhaulMetric> {
        val out = mutableListOf<GlobalBackhaulMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalBackhaulMetricStore(): GlobalBackhaulMetricStore {
    return MemoryGlobalBackhaulMetricStore(
        mutableMapOf<String, GlobalBackhaulMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalBackhaulMetricStore(
    var inner: GlobalBackhaulMetricStore,
    var cache: TtlCache<GlobalBackhaulMetric>,
    var hits: Int,
    var misses: Int,
) : GlobalBackhaulMetricStore {
    override fun find(code: String): GlobalBackhaulMetric? {
        val hit = (cache.get(code) as GlobalBackhaulMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalBackhaulMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalBackhaulMetric> = inner.list()
}

fun newCachedGlobalBackhaulMetricStore(inner: GlobalBackhaulMetricStore, ttl: Int, clock: Clock): GlobalBackhaulMetricStore {
    return CachedGlobalBackhaulMetricStore(
        inner = inner,
        cache = newTtlCache<GlobalBackhaulMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global backhaul metrics. Empty fields match all. */
data class GlobalBackhaulMetricQuery(
    var locale: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalBackhaulMetric): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalBackhaulMetric>): MutableList<GlobalBackhaulMetric> {
        val matched = filter(items) { candidate: GlobalBackhaulMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalBackhaulMetrics(store: GlobalBackhaulMetricStore, query: GlobalBackhaulMetricQuery): MutableList<GlobalBackhaulMetric> {
    return query.apply(store.list())
}
