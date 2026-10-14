package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled clearance variances by code. */
interface ScheduledClearanceVarianceStore {
    fun find(code: String): ScheduledClearanceVariance?
    fun save(item: ScheduledClearanceVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledClearanceVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledClearanceVarianceStore(
    var items: MutableMap<String, ScheduledClearanceVariance>,
    var order: MutableList<String>,
) : ScheduledClearanceVarianceStore {
    override fun find(code: String): ScheduledClearanceVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledClearanceVariance) {
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

    override fun list(): MutableList<ScheduledClearanceVariance> {
        val out = mutableListOf<ScheduledClearanceVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledClearanceVarianceStore(): ScheduledClearanceVarianceStore {
    return MemoryScheduledClearanceVarianceStore(
        mutableMapOf<String, ScheduledClearanceVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledClearanceVarianceStore(
    var inner: ScheduledClearanceVarianceStore,
    var cache: TtlCache<ScheduledClearanceVariance>,
    var hits: Int,
    var misses: Int,
) : ScheduledClearanceVarianceStore {
    override fun find(code: String): ScheduledClearanceVariance? {
        val hit = (cache.get(code) as ScheduledClearanceVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledClearanceVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledClearanceVariance> = inner.list()
}

fun newCachedScheduledClearanceVarianceStore(inner: ScheduledClearanceVarianceStore, ttl: Int, clock: Clock): ScheduledClearanceVarianceStore {
    return CachedScheduledClearanceVarianceStore(
        inner = inner,
        cache = newTtlCache<ScheduledClearanceVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled clearance variances. Empty fields match all. */
data class ScheduledClearanceVarianceQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledClearanceVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledClearanceVariance>): MutableList<ScheduledClearanceVariance> {
        val matched = filter(items) { candidate: ScheduledClearanceVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledClearanceVariances(store: ScheduledClearanceVarianceStore, query: ScheduledClearanceVarianceQuery): MutableList<ScheduledClearanceVariance> {
    return query.apply(store.list())
}
