package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period variances by code. */
interface DutyPeriodVarianceStore {
    fun find(code: String): DutyPeriodVariance?
    fun save(item: DutyPeriodVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodVarianceStore(
    var items: MutableMap<String, DutyPeriodVariance>,
    var order: MutableList<String>,
) : DutyPeriodVarianceStore {
    override fun find(code: String): DutyPeriodVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodVariance) {
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

    override fun list(): MutableList<DutyPeriodVariance> {
        val out = mutableListOf<DutyPeriodVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodVarianceStore(): DutyPeriodVarianceStore {
    return MemoryDutyPeriodVarianceStore(mutableMapOf<String, DutyPeriodVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodVarianceStore(
    var inner: DutyPeriodVarianceStore,
    var cache: TtlCache<DutyPeriodVariance>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodVarianceStore {
    override fun find(code: String): DutyPeriodVariance? {
        val hit = (cache.get(code) as DutyPeriodVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodVariance> = inner.list()
}

fun newCachedDutyPeriodVarianceStore(inner: DutyPeriodVarianceStore, ttl: Int, clock: Clock): DutyPeriodVarianceStore {
    return CachedDutyPeriodVarianceStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period variances. Empty fields match all. */
data class DutyPeriodVarianceQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodVariance>): MutableList<DutyPeriodVariance> {
        val matched = filter(items) { candidate: DutyPeriodVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodVariances(store: DutyPeriodVarianceStore, query: DutyPeriodVarianceQuery): MutableList<DutyPeriodVariance> {
    return query.apply(store.list())
}
