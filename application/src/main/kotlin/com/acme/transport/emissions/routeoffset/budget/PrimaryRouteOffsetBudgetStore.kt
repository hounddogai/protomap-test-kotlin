package com.acme.transport.emissions.routeoffset.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary route offset budgets by code. */
interface PrimaryRouteOffsetBudgetStore {
    fun find(code: String): PrimaryRouteOffsetBudget?
    fun save(item: PrimaryRouteOffsetBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRouteOffsetBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRouteOffsetBudgetStore(
    var items: MutableMap<String, PrimaryRouteOffsetBudget>,
    var order: MutableList<String>,
) : PrimaryRouteOffsetBudgetStore {
    override fun find(code: String): PrimaryRouteOffsetBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRouteOffsetBudget) {
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

    override fun list(): MutableList<PrimaryRouteOffsetBudget> {
        val out = mutableListOf<PrimaryRouteOffsetBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRouteOffsetBudgetStore(): PrimaryRouteOffsetBudgetStore {
    return MemoryPrimaryRouteOffsetBudgetStore(
        mutableMapOf<String, PrimaryRouteOffsetBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRouteOffsetBudgetStore(
    var inner: PrimaryRouteOffsetBudgetStore,
    var cache: TtlCache<PrimaryRouteOffsetBudget>,
    var hits: Int,
    var misses: Int,
) : PrimaryRouteOffsetBudgetStore {
    override fun find(code: String): PrimaryRouteOffsetBudget? {
        val hit = (cache.get(code) as PrimaryRouteOffsetBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRouteOffsetBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRouteOffsetBudget> = inner.list()
}

fun newCachedPrimaryRouteOffsetBudgetStore(inner: PrimaryRouteOffsetBudgetStore, ttl: Int, clock: Clock): PrimaryRouteOffsetBudgetStore {
    return CachedPrimaryRouteOffsetBudgetStore(
        inner = inner,
        cache = newTtlCache<PrimaryRouteOffsetBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary route offset budgets. Empty fields match all. */
data class PrimaryRouteOffsetBudgetQuery(
    var country: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRouteOffsetBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRouteOffsetBudget>): MutableList<PrimaryRouteOffsetBudget> {
        val matched = filter(items) { candidate: PrimaryRouteOffsetBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRouteOffsetBudgets(store: PrimaryRouteOffsetBudgetStore, query: PrimaryRouteOffsetBudgetQuery): MutableList<PrimaryRouteOffsetBudget> {
    return query.apply(store.list())
}
