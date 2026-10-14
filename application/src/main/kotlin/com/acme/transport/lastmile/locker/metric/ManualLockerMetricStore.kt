package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual locker metrics by code. */
interface ManualLockerMetricStore {
    fun find(code: String): ManualLockerMetric?
    fun save(item: ManualLockerMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualLockerMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualLockerMetricStore(
    var items: MutableMap<String, ManualLockerMetric>,
    var order: MutableList<String>,
) : ManualLockerMetricStore {
    override fun find(code: String): ManualLockerMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualLockerMetric) {
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

    override fun list(): MutableList<ManualLockerMetric> {
        val out = mutableListOf<ManualLockerMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualLockerMetricStore(): ManualLockerMetricStore {
    return MemoryManualLockerMetricStore(mutableMapOf<String, ManualLockerMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualLockerMetricStore(
    var inner: ManualLockerMetricStore,
    var cache: TtlCache<ManualLockerMetric>,
    var hits: Int,
    var misses: Int,
) : ManualLockerMetricStore {
    override fun find(code: String): ManualLockerMetric? {
        val hit = (cache.get(code) as ManualLockerMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualLockerMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualLockerMetric> = inner.list()
}

fun newCachedManualLockerMetricStore(inner: ManualLockerMetricStore, ttl: Int, clock: Clock): ManualLockerMetricStore {
    return CachedManualLockerMetricStore(
        inner = inner,
        cache = newTtlCache<ManualLockerMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual locker metrics. Empty fields match all. */
data class ManualLockerMetricQuery(
    var source: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualLockerMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualLockerMetric>): MutableList<ManualLockerMetric> {
        val matched = filter(items) { candidate: ManualLockerMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualLockerMetrics(store: ManualLockerMetricStore, query: ManualLockerMetricQuery): MutableList<ManualLockerMetric> {
    return query.apply(store.list())
}
