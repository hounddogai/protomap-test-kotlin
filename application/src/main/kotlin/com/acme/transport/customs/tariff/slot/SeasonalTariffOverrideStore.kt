package com.acme.transport.customs.tariff.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal tariff overrides by code. */
interface SeasonalTariffOverrideStore {
    fun find(code: String): SeasonalTariffOverride?
    fun save(item: SeasonalTariffOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTariffOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTariffOverrideStore(
    var items: MutableMap<String, SeasonalTariffOverride>,
    var order: MutableList<String>,
) : SeasonalTariffOverrideStore {
    override fun find(code: String): SeasonalTariffOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTariffOverride) {
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

    override fun list(): MutableList<SeasonalTariffOverride> {
        val out = mutableListOf<SeasonalTariffOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTariffOverrideStore(): SeasonalTariffOverrideStore {
    return MemorySeasonalTariffOverrideStore(
        mutableMapOf<String, SeasonalTariffOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTariffOverrideStore(
    var inner: SeasonalTariffOverrideStore,
    var cache: TtlCache<SeasonalTariffOverride>,
    var hits: Int,
    var misses: Int,
) : SeasonalTariffOverrideStore {
    override fun find(code: String): SeasonalTariffOverride? {
        val hit = (cache.get(code) as SeasonalTariffOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTariffOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTariffOverride> = inner.list()
}

fun newCachedSeasonalTariffOverrideStore(inner: SeasonalTariffOverrideStore, ttl: Int, clock: Clock): SeasonalTariffOverrideStore {
    return CachedSeasonalTariffOverrideStore(
        inner = inner,
        cache = newTtlCache<SeasonalTariffOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tariff overrides. Empty fields match all. */
data class SeasonalTariffOverrideQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTariffOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTariffOverride>): MutableList<SeasonalTariffOverride> {
        val matched = filter(items) { candidate: SeasonalTariffOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTariffOverrides(store: SeasonalTariffOverrideStore, query: SeasonalTariffOverrideQuery): MutableList<SeasonalTariffOverride> {
    return query.apply(store.list())
}
