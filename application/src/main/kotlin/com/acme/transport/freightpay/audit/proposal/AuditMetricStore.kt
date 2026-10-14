package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit metrics by code. */
interface AuditMetricStore {
    fun find(code: String): AuditMetric?
    fun save(item: AuditMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditMetricStore(
    var items: MutableMap<String, AuditMetric>,
    var order: MutableList<String>,
) : AuditMetricStore {
    override fun find(code: String): AuditMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditMetric) {
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

    override fun list(): MutableList<AuditMetric> {
        val out = mutableListOf<AuditMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditMetricStore(): AuditMetricStore {
    return MemoryAuditMetricStore(mutableMapOf<String, AuditMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditMetricStore(
    var inner: AuditMetricStore,
    var cache: TtlCache<AuditMetric>,
    var hits: Int,
    var misses: Int,
) : AuditMetricStore {
    override fun find(code: String): AuditMetric? {
        val hit = (cache.get(code) as AuditMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditMetric> = inner.list()
}

fun newCachedAuditMetricStore(inner: AuditMetricStore, ttl: Int, clock: Clock): AuditMetricStore {
    return CachedAuditMetricStore(
        inner = inner,
        cache = newTtlCache<AuditMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit metrics. Empty fields match all. */
data class AuditMetricQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditMetric>): MutableList<AuditMetric> {
        val matched = filter(items) { candidate: AuditMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditMetrics(store: AuditMetricStore, query: AuditMetricQuery): MutableList<AuditMetric> {
    return query.apply(store.list())
}
