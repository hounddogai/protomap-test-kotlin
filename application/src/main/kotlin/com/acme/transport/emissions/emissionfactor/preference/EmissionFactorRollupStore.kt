package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor rollups by code. */
interface EmissionFactorRollupStore {
    fun find(code: String): EmissionFactorRollup?
    fun save(item: EmissionFactorRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorRollupStore(
    var items: MutableMap<String, EmissionFactorRollup>,
    var order: MutableList<String>,
) : EmissionFactorRollupStore {
    override fun find(code: String): EmissionFactorRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorRollup) {
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

    override fun list(): MutableList<EmissionFactorRollup> {
        val out = mutableListOf<EmissionFactorRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorRollupStore(): EmissionFactorRollupStore {
    return MemoryEmissionFactorRollupStore(
        mutableMapOf<String, EmissionFactorRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorRollupStore(
    var inner: EmissionFactorRollupStore,
    var cache: TtlCache<EmissionFactorRollup>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorRollupStore {
    override fun find(code: String): EmissionFactorRollup? {
        val hit = (cache.get(code) as EmissionFactorRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorRollup> = inner.list()
}

fun newCachedEmissionFactorRollupStore(inner: EmissionFactorRollupStore, ttl: Int, clock: Clock): EmissionFactorRollupStore {
    return CachedEmissionFactorRollupStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor rollups. Empty fields match all. */
data class EmissionFactorRollupQuery(
    var unit: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorRollup>): MutableList<EmissionFactorRollup> {
        val matched = filter(items) { candidate: EmissionFactorRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorRollups(store: EmissionFactorRollupStore, query: EmissionFactorRollupQuery): MutableList<EmissionFactorRollup> {
    return query.apply(store.list())
}
