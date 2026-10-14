package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending duty period variances by code. */
interface PendingDutyPeriodVarianceStore {
    fun find(code: String): PendingDutyPeriodVariance?
    fun save(item: PendingDutyPeriodVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDutyPeriodVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDutyPeriodVarianceStore(
    var items: MutableMap<String, PendingDutyPeriodVariance>,
    var order: MutableList<String>,
) : PendingDutyPeriodVarianceStore {
    override fun find(code: String): PendingDutyPeriodVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDutyPeriodVariance) {
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

    override fun list(): MutableList<PendingDutyPeriodVariance> {
        val out = mutableListOf<PendingDutyPeriodVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDutyPeriodVarianceStore(): PendingDutyPeriodVarianceStore {
    return MemoryPendingDutyPeriodVarianceStore(
        mutableMapOf<String, PendingDutyPeriodVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDutyPeriodVarianceStore(
    var inner: PendingDutyPeriodVarianceStore,
    var cache: TtlCache<PendingDutyPeriodVariance>,
    var hits: Int,
    var misses: Int,
) : PendingDutyPeriodVarianceStore {
    override fun find(code: String): PendingDutyPeriodVariance? {
        val hit = (cache.get(code) as PendingDutyPeriodVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDutyPeriodVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDutyPeriodVariance> = inner.list()
}

fun newCachedPendingDutyPeriodVarianceStore(inner: PendingDutyPeriodVarianceStore, ttl: Int, clock: Clock): PendingDutyPeriodVarianceStore {
    return CachedPendingDutyPeriodVarianceStore(
        inner = inner,
        cache = newTtlCache<PendingDutyPeriodVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending duty period variances. Empty fields match all. */
data class PendingDutyPeriodVarianceQuery(
    var locale: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDutyPeriodVariance): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDutyPeriodVariance>): MutableList<PendingDutyPeriodVariance> {
        val matched = filter(items) { candidate: PendingDutyPeriodVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDutyPeriodVariances(store: PendingDutyPeriodVarianceStore, query: PendingDutyPeriodVarianceQuery): MutableList<PendingDutyPeriodVariance> {
    return query.apply(store.list())
}
