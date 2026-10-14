package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional trailer yard plans by code. */
interface RegionalTrailerYardPlanStore {
    fun find(code: String): RegionalTrailerYardPlan?
    fun save(item: RegionalTrailerYardPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerYardPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerYardPlanStore(
    var items: MutableMap<String, RegionalTrailerYardPlan>,
    var order: MutableList<String>,
) : RegionalTrailerYardPlanStore {
    override fun find(code: String): RegionalTrailerYardPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerYardPlan) {
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

    override fun list(): MutableList<RegionalTrailerYardPlan> {
        val out = mutableListOf<RegionalTrailerYardPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerYardPlanStore(): RegionalTrailerYardPlanStore {
    return MemoryRegionalTrailerYardPlanStore(
        mutableMapOf<String, RegionalTrailerYardPlan>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerYardPlanStore(
    var inner: RegionalTrailerYardPlanStore,
    var cache: TtlCache<RegionalTrailerYardPlan>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerYardPlanStore {
    override fun find(code: String): RegionalTrailerYardPlan? {
        val hit = (cache.get(code) as RegionalTrailerYardPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerYardPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerYardPlan> = inner.list()
}

fun newCachedRegionalTrailerYardPlanStore(inner: RegionalTrailerYardPlanStore, ttl: Int, clock: Clock): RegionalTrailerYardPlanStore {
    return CachedRegionalTrailerYardPlanStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerYardPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer yard plans. Empty fields match all. */
data class RegionalTrailerYardPlanQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerYardPlan): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalTrailerYardPlan>): MutableList<RegionalTrailerYardPlan> {
        val matched = filter(items) { candidate: RegionalTrailerYardPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerYardPlans(store: RegionalTrailerYardPlanStore, query: RegionalTrailerYardPlanQuery): MutableList<RegionalTrailerYardPlan> {
    return query.apply(store.list())
}
