package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor rules by code. */
interface EmissionFactorRuleStore {
    fun find(code: String): EmissionFactorRule?
    fun save(item: EmissionFactorRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorRuleStore(
    var items: MutableMap<String, EmissionFactorRule>,
    var order: MutableList<String>,
) : EmissionFactorRuleStore {
    override fun find(code: String): EmissionFactorRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorRule) {
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

    override fun list(): MutableList<EmissionFactorRule> {
        val out = mutableListOf<EmissionFactorRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorRuleStore(): EmissionFactorRuleStore {
    return MemoryEmissionFactorRuleStore(mutableMapOf<String, EmissionFactorRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorRuleStore(
    var inner: EmissionFactorRuleStore,
    var cache: TtlCache<EmissionFactorRule>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorRuleStore {
    override fun find(code: String): EmissionFactorRule? {
        val hit = (cache.get(code) as EmissionFactorRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorRule> = inner.list()
}

fun newCachedEmissionFactorRuleStore(inner: EmissionFactorRuleStore, ttl: Int, clock: Clock): EmissionFactorRuleStore {
    return CachedEmissionFactorRuleStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorRule>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor rules. Empty fields match all. */
data class EmissionFactorRuleQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorRule): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorRule>): MutableList<EmissionFactorRule> {
        val matched = filter(items) { candidate: EmissionFactorRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorRules(store: EmissionFactorRuleStore, query: EmissionFactorRuleQuery): MutableList<EmissionFactorRule> {
    return query.apply(store.list())
}
