package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster metrics by code. */
interface RosterMetricStore {
    fun find(code: String): RosterMetric?
    fun save(item: RosterMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterMetricStore(
    var items: MutableMap<String, RosterMetric>,
    var order: MutableList<String>,
) : RosterMetricStore {
    override fun find(code: String): RosterMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterMetric) {
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

    override fun list(): MutableList<RosterMetric> {
        val out = mutableListOf<RosterMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterMetricStore(): RosterMetricStore {
    return MemoryRosterMetricStore(mutableMapOf<String, RosterMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterMetricStore(
    var inner: RosterMetricStore,
    var cache: TtlCache<RosterMetric>,
    var hits: Int,
    var misses: Int,
) : RosterMetricStore {
    override fun find(code: String): RosterMetric? {
        val hit = (cache.get(code) as RosterMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterMetric> = inner.list()
}

fun newCachedRosterMetricStore(inner: RosterMetricStore, ttl: Int, clock: Clock): RosterMetricStore {
    return CachedRosterMetricStore(
        inner = inner,
        cache = newTtlCache<RosterMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster metrics. Empty fields match all. */
data class RosterMetricQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterMetric>): MutableList<RosterMetric> {
        val matched = filter(items) { candidate: RosterMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterMetrics(store: RosterMetricStore, query: RosterMetricQuery): MutableList<RosterMetric> {
    return query.apply(store.list())
}
