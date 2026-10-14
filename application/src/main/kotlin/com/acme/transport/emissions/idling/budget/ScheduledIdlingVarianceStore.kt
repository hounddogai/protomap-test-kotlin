package com.acme.transport.emissions.idling.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled idling variances by code. */
interface ScheduledIdlingVarianceStore {
    fun find(code: String): ScheduledIdlingVariance?
    fun save(item: ScheduledIdlingVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledIdlingVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledIdlingVarianceStore(
    var items: MutableMap<String, ScheduledIdlingVariance>,
    var order: MutableList<String>,
) : ScheduledIdlingVarianceStore {
    override fun find(code: String): ScheduledIdlingVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledIdlingVariance) {
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

    override fun list(): MutableList<ScheduledIdlingVariance> {
        val out = mutableListOf<ScheduledIdlingVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledIdlingVarianceStore(): ScheduledIdlingVarianceStore {
    return MemoryScheduledIdlingVarianceStore(
        mutableMapOf<String, ScheduledIdlingVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledIdlingVarianceStore(
    var inner: ScheduledIdlingVarianceStore,
    var cache: TtlCache<ScheduledIdlingVariance>,
    var hits: Int,
    var misses: Int,
) : ScheduledIdlingVarianceStore {
    override fun find(code: String): ScheduledIdlingVariance? {
        val hit = (cache.get(code) as ScheduledIdlingVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledIdlingVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledIdlingVariance> = inner.list()
}

fun newCachedScheduledIdlingVarianceStore(inner: ScheduledIdlingVarianceStore, ttl: Int, clock: Clock): ScheduledIdlingVarianceStore {
    return CachedScheduledIdlingVarianceStore(
        inner = inner,
        cache = newTtlCache<ScheduledIdlingVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled idling variances. Empty fields match all. */
data class ScheduledIdlingVarianceQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledIdlingVariance): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<ScheduledIdlingVariance>): MutableList<ScheduledIdlingVariance> {
        val matched = filter(items) { candidate: ScheduledIdlingVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledIdlingVariances(store: ScheduledIdlingVarianceStore, query: ScheduledIdlingVarianceQuery): MutableList<ScheduledIdlingVariance> {
    return query.apply(store.list())
}
