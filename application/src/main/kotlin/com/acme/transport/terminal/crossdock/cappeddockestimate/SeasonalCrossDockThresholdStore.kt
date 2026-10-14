package com.acme.transport.terminal.crossdock.cappeddockestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal cross dock thresholds by code. */
interface SeasonalCrossDockThresholdStore {
    fun find(code: String): SeasonalCrossDockThreshold?
    fun save(item: SeasonalCrossDockThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCrossDockThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCrossDockThresholdStore(
    var items: MutableMap<String, SeasonalCrossDockThreshold>,
    var order: MutableList<String>,
) : SeasonalCrossDockThresholdStore {
    override fun find(code: String): SeasonalCrossDockThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCrossDockThreshold) {
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

    override fun list(): MutableList<SeasonalCrossDockThreshold> {
        val out = mutableListOf<SeasonalCrossDockThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCrossDockThresholdStore(): SeasonalCrossDockThresholdStore {
    return MemorySeasonalCrossDockThresholdStore(
        mutableMapOf<String, SeasonalCrossDockThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCrossDockThresholdStore(
    var inner: SeasonalCrossDockThresholdStore,
    var cache: TtlCache<SeasonalCrossDockThreshold>,
    var hits: Int,
    var misses: Int,
) : SeasonalCrossDockThresholdStore {
    override fun find(code: String): SeasonalCrossDockThreshold? {
        val hit = (cache.get(code) as SeasonalCrossDockThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCrossDockThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCrossDockThreshold> = inner.list()
}

fun newCachedSeasonalCrossDockThresholdStore(inner: SeasonalCrossDockThresholdStore, ttl: Int, clock: Clock): SeasonalCrossDockThresholdStore {
    return CachedSeasonalCrossDockThresholdStore(
        inner = inner,
        cache = newTtlCache<SeasonalCrossDockThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal cross dock thresholds. Empty fields match all. */
data class SeasonalCrossDockThresholdQuery(
    var depot: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCrossDockThreshold): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<SeasonalCrossDockThreshold>): MutableList<SeasonalCrossDockThreshold> {
        val matched = filter(items) { candidate: SeasonalCrossDockThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCrossDockThresholds(store: SeasonalCrossDockThresholdStore, query: SeasonalCrossDockThresholdQuery): MutableList<SeasonalCrossDockThreshold> {
    return query.apply(store.list())
}
