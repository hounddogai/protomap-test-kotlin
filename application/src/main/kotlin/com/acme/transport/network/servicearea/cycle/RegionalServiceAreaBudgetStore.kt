package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional service area budgets by code. */
interface RegionalServiceAreaBudgetStore {
    fun find(code: String): RegionalServiceAreaBudget?
    fun save(item: RegionalServiceAreaBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServiceAreaBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServiceAreaBudgetStore(
    var items: MutableMap<String, RegionalServiceAreaBudget>,
    var order: MutableList<String>,
) : RegionalServiceAreaBudgetStore {
    override fun find(code: String): RegionalServiceAreaBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServiceAreaBudget) {
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

    override fun list(): MutableList<RegionalServiceAreaBudget> {
        val out = mutableListOf<RegionalServiceAreaBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServiceAreaBudgetStore(): RegionalServiceAreaBudgetStore {
    return MemoryRegionalServiceAreaBudgetStore(
        mutableMapOf<String, RegionalServiceAreaBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServiceAreaBudgetStore(
    var inner: RegionalServiceAreaBudgetStore,
    var cache: TtlCache<RegionalServiceAreaBudget>,
    var hits: Int,
    var misses: Int,
) : RegionalServiceAreaBudgetStore {
    override fun find(code: String): RegionalServiceAreaBudget? {
        val hit = (cache.get(code) as RegionalServiceAreaBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServiceAreaBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServiceAreaBudget> = inner.list()
}

fun newCachedRegionalServiceAreaBudgetStore(inner: RegionalServiceAreaBudgetStore, ttl: Int, clock: Clock): RegionalServiceAreaBudgetStore {
    return CachedRegionalServiceAreaBudgetStore(
        inner = inner,
        cache = newTtlCache<RegionalServiceAreaBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service area budgets. Empty fields match all. */
data class RegionalServiceAreaBudgetQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServiceAreaBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalServiceAreaBudget>): MutableList<RegionalServiceAreaBudget> {
        val matched = filter(items) { candidate: RegionalServiceAreaBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServiceAreaBudgets(store: RegionalServiceAreaBudgetStore, query: RegionalServiceAreaBudgetQuery): MutableList<RegionalServiceAreaBudget> {
    return query.apply(store.list())
}
