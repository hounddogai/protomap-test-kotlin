package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal service plan baselines by code. */
interface SeasonalServicePlanBaselineStore {
    fun find(code: String): SeasonalServicePlanBaseline?
    fun save(item: SeasonalServicePlanBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalServicePlanBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalServicePlanBaselineStore(
    var items: MutableMap<String, SeasonalServicePlanBaseline>,
    var order: MutableList<String>,
) : SeasonalServicePlanBaselineStore {
    override fun find(code: String): SeasonalServicePlanBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalServicePlanBaseline) {
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

    override fun list(): MutableList<SeasonalServicePlanBaseline> {
        val out = mutableListOf<SeasonalServicePlanBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalServicePlanBaselineStore(): SeasonalServicePlanBaselineStore {
    return MemorySeasonalServicePlanBaselineStore(
        mutableMapOf<String, SeasonalServicePlanBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalServicePlanBaselineStore(
    var inner: SeasonalServicePlanBaselineStore,
    var cache: TtlCache<SeasonalServicePlanBaseline>,
    var hits: Int,
    var misses: Int,
) : SeasonalServicePlanBaselineStore {
    override fun find(code: String): SeasonalServicePlanBaseline? {
        val hit = (cache.get(code) as SeasonalServicePlanBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalServicePlanBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalServicePlanBaseline> = inner.list()
}

fun newCachedSeasonalServicePlanBaselineStore(inner: SeasonalServicePlanBaselineStore, ttl: Int, clock: Clock): SeasonalServicePlanBaselineStore {
    return CachedSeasonalServicePlanBaselineStore(
        inner = inner,
        cache = newTtlCache<SeasonalServicePlanBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal service plan baselines. Empty fields match all. */
data class SeasonalServicePlanBaselineQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalServicePlanBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalServicePlanBaseline>): MutableList<SeasonalServicePlanBaseline> {
        val matched = filter(items) { candidate: SeasonalServicePlanBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalServicePlanBaselines(store: SeasonalServicePlanBaselineStore, query: SeasonalServicePlanBaselineQuery): MutableList<SeasonalServicePlanBaseline> {
    return query.apply(store.list())
}
