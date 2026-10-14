package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal fuel burn groups by code. */
interface SeasonalFuelBurnGroupStore {
    fun find(code: String): SeasonalFuelBurnGroup?
    fun save(item: SeasonalFuelBurnGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFuelBurnGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFuelBurnGroupStore(
    var items: MutableMap<String, SeasonalFuelBurnGroup>,
    var order: MutableList<String>,
) : SeasonalFuelBurnGroupStore {
    override fun find(code: String): SeasonalFuelBurnGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFuelBurnGroup) {
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

    override fun list(): MutableList<SeasonalFuelBurnGroup> {
        val out = mutableListOf<SeasonalFuelBurnGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFuelBurnGroupStore(): SeasonalFuelBurnGroupStore {
    return MemorySeasonalFuelBurnGroupStore(
        mutableMapOf<String, SeasonalFuelBurnGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFuelBurnGroupStore(
    var inner: SeasonalFuelBurnGroupStore,
    var cache: TtlCache<SeasonalFuelBurnGroup>,
    var hits: Int,
    var misses: Int,
) : SeasonalFuelBurnGroupStore {
    override fun find(code: String): SeasonalFuelBurnGroup? {
        val hit = (cache.get(code) as SeasonalFuelBurnGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFuelBurnGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFuelBurnGroup> = inner.list()
}

fun newCachedSeasonalFuelBurnGroupStore(inner: SeasonalFuelBurnGroupStore, ttl: Int, clock: Clock): SeasonalFuelBurnGroupStore {
    return CachedSeasonalFuelBurnGroupStore(
        inner = inner,
        cache = newTtlCache<SeasonalFuelBurnGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal fuel burn groups. Empty fields match all. */
data class SeasonalFuelBurnGroupQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFuelBurnGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFuelBurnGroup>): MutableList<SeasonalFuelBurnGroup> {
        val matched = filter(items) { candidate: SeasonalFuelBurnGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFuelBurnGroups(store: SeasonalFuelBurnGroupStore, query: SeasonalFuelBurnGroupQuery): MutableList<SeasonalFuelBurnGroup> {
    return query.apply(store.list())
}
