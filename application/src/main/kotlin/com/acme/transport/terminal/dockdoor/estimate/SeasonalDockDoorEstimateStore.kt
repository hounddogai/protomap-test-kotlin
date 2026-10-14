package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dock door estimates by code. */
interface SeasonalDockDoorEstimateStore {
    fun find(code: String): SeasonalDockDoorEstimate?
    fun save(item: SeasonalDockDoorEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDockDoorEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDockDoorEstimateStore(
    var items: MutableMap<String, SeasonalDockDoorEstimate>,
    var order: MutableList<String>,
) : SeasonalDockDoorEstimateStore {
    override fun find(code: String): SeasonalDockDoorEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDockDoorEstimate) {
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

    override fun list(): MutableList<SeasonalDockDoorEstimate> {
        val out = mutableListOf<SeasonalDockDoorEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDockDoorEstimateStore(): SeasonalDockDoorEstimateStore {
    return MemorySeasonalDockDoorEstimateStore(
        mutableMapOf<String, SeasonalDockDoorEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDockDoorEstimateStore(
    var inner: SeasonalDockDoorEstimateStore,
    var cache: TtlCache<SeasonalDockDoorEstimate>,
    var hits: Int,
    var misses: Int,
) : SeasonalDockDoorEstimateStore {
    override fun find(code: String): SeasonalDockDoorEstimate? {
        val hit = (cache.get(code) as SeasonalDockDoorEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDockDoorEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDockDoorEstimate> = inner.list()
}

fun newCachedSeasonalDockDoorEstimateStore(inner: SeasonalDockDoorEstimateStore, ttl: Int, clock: Clock): SeasonalDockDoorEstimateStore {
    return CachedSeasonalDockDoorEstimateStore(
        inner = inner,
        cache = newTtlCache<SeasonalDockDoorEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dock door estimates. Empty fields match all. */
data class SeasonalDockDoorEstimateQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDockDoorEstimate): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<SeasonalDockDoorEstimate>): MutableList<SeasonalDockDoorEstimate> {
        val matched = filter(items) { candidate: SeasonalDockDoorEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDockDoorEstimates(store: SeasonalDockDoorEstimateStore, query: SeasonalDockDoorEstimateQuery): MutableList<SeasonalDockDoorEstimate> {
    return query.apply(store.list())
}
