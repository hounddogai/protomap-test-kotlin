package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal idling profiles by code. */
interface SeasonalIdlingProfileStore {
    fun find(code: String): SeasonalIdlingProfile?
    fun save(item: SeasonalIdlingProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalIdlingProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalIdlingProfileStore(
    var items: MutableMap<String, SeasonalIdlingProfile>,
    var order: MutableList<String>,
) : SeasonalIdlingProfileStore {
    override fun find(code: String): SeasonalIdlingProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalIdlingProfile) {
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

    override fun list(): MutableList<SeasonalIdlingProfile> {
        val out = mutableListOf<SeasonalIdlingProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalIdlingProfileStore(): SeasonalIdlingProfileStore {
    return MemorySeasonalIdlingProfileStore(
        mutableMapOf<String, SeasonalIdlingProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalIdlingProfileStore(
    var inner: SeasonalIdlingProfileStore,
    var cache: TtlCache<SeasonalIdlingProfile>,
    var hits: Int,
    var misses: Int,
) : SeasonalIdlingProfileStore {
    override fun find(code: String): SeasonalIdlingProfile? {
        val hit = (cache.get(code) as SeasonalIdlingProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalIdlingProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalIdlingProfile> = inner.list()
}

fun newCachedSeasonalIdlingProfileStore(inner: SeasonalIdlingProfileStore, ttl: Int, clock: Clock): SeasonalIdlingProfileStore {
    return CachedSeasonalIdlingProfileStore(
        inner = inner,
        cache = newTtlCache<SeasonalIdlingProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal idling profiles. Empty fields match all. */
data class SeasonalIdlingProfileQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalIdlingProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalIdlingProfile>): MutableList<SeasonalIdlingProfile> {
        val matched = filter(items) { candidate: SeasonalIdlingProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalIdlingProfiles(store: SeasonalIdlingProfileStore, query: SeasonalIdlingProfileQuery): MutableList<SeasonalIdlingProfile> {
    return query.apply(store.list())
}
