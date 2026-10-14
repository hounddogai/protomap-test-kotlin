package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive emission factor groups by code. */
interface AdaptiveEmissionFactorGroupStore {
    fun find(code: String): AdaptiveEmissionFactorGroup?
    fun save(item: AdaptiveEmissionFactorGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveEmissionFactorGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveEmissionFactorGroupStore(
    var items: MutableMap<String, AdaptiveEmissionFactorGroup>,
    var order: MutableList<String>,
) : AdaptiveEmissionFactorGroupStore {
    override fun find(code: String): AdaptiveEmissionFactorGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveEmissionFactorGroup) {
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

    override fun list(): MutableList<AdaptiveEmissionFactorGroup> {
        val out = mutableListOf<AdaptiveEmissionFactorGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveEmissionFactorGroupStore(): AdaptiveEmissionFactorGroupStore {
    return MemoryAdaptiveEmissionFactorGroupStore(
        mutableMapOf<String, AdaptiveEmissionFactorGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveEmissionFactorGroupStore(
    var inner: AdaptiveEmissionFactorGroupStore,
    var cache: TtlCache<AdaptiveEmissionFactorGroup>,
    var hits: Int,
    var misses: Int,
) : AdaptiveEmissionFactorGroupStore {
    override fun find(code: String): AdaptiveEmissionFactorGroup? {
        val hit = (cache.get(code) as AdaptiveEmissionFactorGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveEmissionFactorGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveEmissionFactorGroup> = inner.list()
}

fun newCachedAdaptiveEmissionFactorGroupStore(inner: AdaptiveEmissionFactorGroupStore, ttl: Int, clock: Clock): AdaptiveEmissionFactorGroupStore {
    return CachedAdaptiveEmissionFactorGroupStore(
        inner = inner,
        cache = newTtlCache<AdaptiveEmissionFactorGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive emission factor groups. Empty fields match all. */
data class AdaptiveEmissionFactorGroupQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveEmissionFactorGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveEmissionFactorGroup>): MutableList<AdaptiveEmissionFactorGroup> {
        val matched = filter(items) { candidate: AdaptiveEmissionFactorGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveEmissionFactorGroups(store: AdaptiveEmissionFactorGroupStore, query: AdaptiveEmissionFactorGroupQuery): MutableList<AdaptiveEmissionFactorGroup> {
    return query.apply(store.list())
}
