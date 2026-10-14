package com.acme.transport.lastmile.redelivery.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal redelivery thresholds by code. */
interface SeasonalRedeliveryThresholdStore {
    fun find(code: String): SeasonalRedeliveryThreshold?
    fun save(item: SeasonalRedeliveryThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRedeliveryThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRedeliveryThresholdStore(
    var items: MutableMap<String, SeasonalRedeliveryThreshold>,
    var order: MutableList<String>,
) : SeasonalRedeliveryThresholdStore {
    override fun find(code: String): SeasonalRedeliveryThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRedeliveryThreshold) {
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

    override fun list(): MutableList<SeasonalRedeliveryThreshold> {
        val out = mutableListOf<SeasonalRedeliveryThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRedeliveryThresholdStore(): SeasonalRedeliveryThresholdStore {
    return MemorySeasonalRedeliveryThresholdStore(
        mutableMapOf<String, SeasonalRedeliveryThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRedeliveryThresholdStore(
    var inner: SeasonalRedeliveryThresholdStore,
    var cache: TtlCache<SeasonalRedeliveryThreshold>,
    var hits: Int,
    var misses: Int,
) : SeasonalRedeliveryThresholdStore {
    override fun find(code: String): SeasonalRedeliveryThreshold? {
        val hit = (cache.get(code) as SeasonalRedeliveryThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRedeliveryThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRedeliveryThreshold> = inner.list()
}

fun newCachedSeasonalRedeliveryThresholdStore(inner: SeasonalRedeliveryThresholdStore, ttl: Int, clock: Clock): SeasonalRedeliveryThresholdStore {
    return CachedSeasonalRedeliveryThresholdStore(
        inner = inner,
        cache = newTtlCache<SeasonalRedeliveryThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal redelivery thresholds. Empty fields match all. */
data class SeasonalRedeliveryThresholdQuery(
    var unit: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRedeliveryThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRedeliveryThreshold>): MutableList<SeasonalRedeliveryThreshold> {
        val matched = filter(items) { candidate: SeasonalRedeliveryThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRedeliveryThresholds(store: SeasonalRedeliveryThresholdStore, query: SeasonalRedeliveryThresholdQuery): MutableList<SeasonalRedeliveryThreshold> {
    return query.apply(store.list())
}
