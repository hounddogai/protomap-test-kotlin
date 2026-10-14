package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal service plan thresholds by code. */
interface SeasonalServicePlanThresholdStore {
    fun find(code: String): SeasonalServicePlanThreshold?
    fun save(item: SeasonalServicePlanThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServicePlanThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServicePlanThresholdStore(
    var items: MutableMap<String, SeasonalServicePlanThreshold>,
    var order: MutableList<String>,
) : SeasonalServicePlanThresholdStore {
    override fun find(code: String): SeasonalServicePlanThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServicePlanThreshold) {
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

    override fun list(): MutableList<SeasonalServicePlanThreshold> {
        val out = mutableListOf<SeasonalServicePlanThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServicePlanThresholdStore(): SeasonalServicePlanThresholdStore {
    return MemorySeasonalServicePlanThresholdStore(
        mutableMapOf<String, SeasonalServicePlanThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServicePlanThresholdStore(
    var inner: SeasonalServicePlanThresholdStore,
    var cache: TtlCache<SeasonalServicePlanThreshold>,
    var hits: Int,
    var misses: Int,
) : SeasonalServicePlanThresholdStore {
    override fun find(code: String): SeasonalServicePlanThreshold? {
        val hit = (cache.get(code) as SeasonalServicePlanThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServicePlanThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServicePlanThreshold> = inner.list()
}

fun newCachedSeasonalServicePlanThresholdStore(inner: SeasonalServicePlanThresholdStore, ttl: Int, clock: Clock): SeasonalServicePlanThresholdStore {
    return CachedSeasonalServicePlanThresholdStore(
        inner = inner,
        cache = newTtlCache<SeasonalServicePlanThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service plan thresholds. Empty fields match all. */
data class SeasonalServicePlanThresholdQuery(
    var segment: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServicePlanThreshold): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalServicePlanThreshold>): MutableList<SeasonalServicePlanThreshold> {
        val matched = filter(items) { candidate: SeasonalServicePlanThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServicePlanThresholds(store: SeasonalServicePlanThresholdStore, query: SeasonalServicePlanThresholdQuery): MutableList<SeasonalServicePlanThreshold> {
    return query.apply(store.list())
}
