package com.acme.transport.crewing.availability.segment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal availability cycles by code. */
interface SeasonalAvailabilityCycleStore {
    fun find(code: String): SeasonalAvailabilityCycle?
    fun save(item: SeasonalAvailabilityCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAvailabilityCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAvailabilityCycleStore(
    var items: MutableMap<String, SeasonalAvailabilityCycle>,
    var order: MutableList<String>,
) : SeasonalAvailabilityCycleStore {
    override fun find(code: String): SeasonalAvailabilityCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAvailabilityCycle) {
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

    override fun list(): MutableList<SeasonalAvailabilityCycle> {
        val out = mutableListOf<SeasonalAvailabilityCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAvailabilityCycleStore(): SeasonalAvailabilityCycleStore {
    return MemorySeasonalAvailabilityCycleStore(
        mutableMapOf<String, SeasonalAvailabilityCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAvailabilityCycleStore(
    var inner: SeasonalAvailabilityCycleStore,
    var cache: TtlCache<SeasonalAvailabilityCycle>,
    var hits: Int,
    var misses: Int,
) : SeasonalAvailabilityCycleStore {
    override fun find(code: String): SeasonalAvailabilityCycle? {
        val hit = (cache.get(code) as SeasonalAvailabilityCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAvailabilityCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAvailabilityCycle> = inner.list()
}

fun newCachedSeasonalAvailabilityCycleStore(inner: SeasonalAvailabilityCycleStore, ttl: Int, clock: Clock): SeasonalAvailabilityCycleStore {
    return CachedSeasonalAvailabilityCycleStore(
        inner = inner,
        cache = newTtlCache<SeasonalAvailabilityCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal availability cycles. Empty fields match all. */
data class SeasonalAvailabilityCycleQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAvailabilityCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAvailabilityCycle>): MutableList<SeasonalAvailabilityCycle> {
        val matched = filter(items) { candidate: SeasonalAvailabilityCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAvailabilityCycles(store: SeasonalAvailabilityCycleStore, query: SeasonalAvailabilityCycleQuery): MutableList<SeasonalAvailabilityCycle> {
    return query.apply(store.list())
}
