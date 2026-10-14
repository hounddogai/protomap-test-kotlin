package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending violation metrics by code. */
interface PendingViolationMetricStore {
    fun find(code: String): PendingViolationMetric?
    fun save(item: PendingViolationMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingViolationMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingViolationMetricStore(
    var items: MutableMap<String, PendingViolationMetric>,
    var order: MutableList<String>,
) : PendingViolationMetricStore {
    override fun find(code: String): PendingViolationMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingViolationMetric) {
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

    override fun list(): MutableList<PendingViolationMetric> {
        val out = mutableListOf<PendingViolationMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingViolationMetricStore(): PendingViolationMetricStore {
    return MemoryPendingViolationMetricStore(
        mutableMapOf<String, PendingViolationMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingViolationMetricStore(
    var inner: PendingViolationMetricStore,
    var cache: TtlCache<PendingViolationMetric>,
    var hits: Int,
    var misses: Int,
) : PendingViolationMetricStore {
    override fun find(code: String): PendingViolationMetric? {
        val hit = (cache.get(code) as PendingViolationMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingViolationMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingViolationMetric> = inner.list()
}

fun newCachedPendingViolationMetricStore(inner: PendingViolationMetricStore, ttl: Int, clock: Clock): PendingViolationMetricStore {
    return CachedPendingViolationMetricStore(
        inner = inner,
        cache = newTtlCache<PendingViolationMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending violation metrics. Empty fields match all. */
data class PendingViolationMetricQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingViolationMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingViolationMetric>): MutableList<PendingViolationMetric> {
        val matched = filter(items) { candidate: PendingViolationMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingViolationMetrics(store: PendingViolationMetricStore, query: PendingViolationMetricQuery): MutableList<PendingViolationMetric> {
    return query.apply(store.list())
}
