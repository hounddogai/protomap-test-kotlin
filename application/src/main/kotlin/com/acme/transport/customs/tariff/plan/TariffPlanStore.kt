package com.acme.transport.customs.tariff.plan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff plans by code. */
interface TariffPlanStore {
    fun find(code: String): TariffPlan?
    fun save(item: TariffPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffPlanStore(
    var items: MutableMap<String, TariffPlan>,
    var order: MutableList<String>,
) : TariffPlanStore {
    override fun find(code: String): TariffPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffPlan) {
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

    override fun list(): MutableList<TariffPlan> {
        val out = mutableListOf<TariffPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffPlanStore(): TariffPlanStore {
    return MemoryTariffPlanStore(mutableMapOf<String, TariffPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffPlanStore(
    var inner: TariffPlanStore,
    var cache: TtlCache<TariffPlan>,
    var hits: Int,
    var misses: Int,
) : TariffPlanStore {
    override fun find(code: String): TariffPlan? {
        val hit = (cache.get(code) as TariffPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffPlan> = inner.list()
}

fun newCachedTariffPlanStore(inner: TariffPlanStore, ttl: Int, clock: Clock): TariffPlanStore {
    return CachedTariffPlanStore(
        inner = inner,
        cache = newTtlCache<TariffPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff plans. Empty fields match all. */
data class TariffPlanQuery(
    var region: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffPlan): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffPlan>): MutableList<TariffPlan> {
        val matched = filter(items) { candidate: TariffPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffPlans(store: TariffPlanStore, query: TariffPlanQuery): MutableList<TariffPlan> {
    return query.apply(store.list())
}
