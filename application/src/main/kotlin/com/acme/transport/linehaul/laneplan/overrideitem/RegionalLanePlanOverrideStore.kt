package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional lane plan overrides by code. */
interface RegionalLanePlanOverrideStore {
    fun find(code: String): RegionalLanePlanOverride?
    fun save(item: RegionalLanePlanOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLanePlanOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLanePlanOverrideStore(
    var items: MutableMap<String, RegionalLanePlanOverride>,
    var order: MutableList<String>,
) : RegionalLanePlanOverrideStore {
    override fun find(code: String): RegionalLanePlanOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLanePlanOverride) {
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

    override fun list(): MutableList<RegionalLanePlanOverride> {
        val out = mutableListOf<RegionalLanePlanOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLanePlanOverrideStore(): RegionalLanePlanOverrideStore {
    return MemoryRegionalLanePlanOverrideStore(
        mutableMapOf<String, RegionalLanePlanOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLanePlanOverrideStore(
    var inner: RegionalLanePlanOverrideStore,
    var cache: TtlCache<RegionalLanePlanOverride>,
    var hits: Int,
    var misses: Int,
) : RegionalLanePlanOverrideStore {
    override fun find(code: String): RegionalLanePlanOverride? {
        val hit = (cache.get(code) as RegionalLanePlanOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLanePlanOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLanePlanOverride> = inner.list()
}

fun newCachedRegionalLanePlanOverrideStore(inner: RegionalLanePlanOverrideStore, ttl: Int, clock: Clock): RegionalLanePlanOverrideStore {
    return CachedRegionalLanePlanOverrideStore(
        inner = inner,
        cache = newTtlCache<RegionalLanePlanOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lane plan overrides. Empty fields match all. */
data class RegionalLanePlanOverrideQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLanePlanOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalLanePlanOverride>): MutableList<RegionalLanePlanOverride> {
        val matched = filter(items) { candidate: RegionalLanePlanOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLanePlanOverrides(store: RegionalLanePlanOverrideStore, query: RegionalLanePlanOverrideQuery): MutableList<RegionalLanePlanOverride> {
    return query.apply(store.list())
}
