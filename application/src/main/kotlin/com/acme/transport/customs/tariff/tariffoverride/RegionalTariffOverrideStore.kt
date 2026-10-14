package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional tariff overrides by code. */
interface RegionalTariffOverrideStore {
    fun find(code: String): RegionalTariffOverride?
    fun save(item: RegionalTariffOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTariffOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTariffOverrideStore(
    var items: MutableMap<String, RegionalTariffOverride>,
    var order: MutableList<String>,
) : RegionalTariffOverrideStore {
    override fun find(code: String): RegionalTariffOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTariffOverride) {
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

    override fun list(): MutableList<RegionalTariffOverride> {
        val out = mutableListOf<RegionalTariffOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTariffOverrideStore(): RegionalTariffOverrideStore {
    return MemoryRegionalTariffOverrideStore(
        mutableMapOf<String, RegionalTariffOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTariffOverrideStore(
    var inner: RegionalTariffOverrideStore,
    var cache: TtlCache<RegionalTariffOverride>,
    var hits: Int,
    var misses: Int,
) : RegionalTariffOverrideStore {
    override fun find(code: String): RegionalTariffOverride? {
        val hit = (cache.get(code) as RegionalTariffOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTariffOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTariffOverride> = inner.list()
}

fun newCachedRegionalTariffOverrideStore(inner: RegionalTariffOverrideStore, ttl: Int, clock: Clock): RegionalTariffOverrideStore {
    return CachedRegionalTariffOverrideStore(
        inner = inner,
        cache = newTtlCache<RegionalTariffOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional tariff overrides. Empty fields match all. */
data class RegionalTariffOverrideQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTariffOverride): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RegionalTariffOverride>): MutableList<RegionalTariffOverride> {
        val matched = filter(items) { candidate: RegionalTariffOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTariffOverrides(store: RegionalTariffOverrideStore, query: RegionalTariffOverrideQuery): MutableList<RegionalTariffOverride> {
    return query.apply(store.list())
}
