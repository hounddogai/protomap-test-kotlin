package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor overrides by code. */
interface EmissionFactorOverrideStore {
    fun find(code: String): EmissionFactorOverride?
    fun save(item: EmissionFactorOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorOverrideStore(
    var items: MutableMap<String, EmissionFactorOverride>,
    var order: MutableList<String>,
) : EmissionFactorOverrideStore {
    override fun find(code: String): EmissionFactorOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorOverride) {
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

    override fun list(): MutableList<EmissionFactorOverride> {
        val out = mutableListOf<EmissionFactorOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorOverrideStore(): EmissionFactorOverrideStore {
    return MemoryEmissionFactorOverrideStore(
        mutableMapOf<String, EmissionFactorOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorOverrideStore(
    var inner: EmissionFactorOverrideStore,
    var cache: TtlCache<EmissionFactorOverride>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorOverrideStore {
    override fun find(code: String): EmissionFactorOverride? {
        val hit = (cache.get(code) as EmissionFactorOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorOverride> = inner.list()
}

fun newCachedEmissionFactorOverrideStore(inner: EmissionFactorOverrideStore, ttl: Int, clock: Clock): EmissionFactorOverrideStore {
    return CachedEmissionFactorOverrideStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor overrides. Empty fields match all. */
data class EmissionFactorOverrideQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorOverride): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorOverride>): MutableList<EmissionFactorOverride> {
        val matched = filter(items) { candidate: EmissionFactorOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorOverrides(store: EmissionFactorOverrideStore, query: EmissionFactorOverrideQuery): MutableList<EmissionFactorOverride> {
    return query.apply(store.list())
}
