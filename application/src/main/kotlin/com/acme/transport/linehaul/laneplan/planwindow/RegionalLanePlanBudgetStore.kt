package com.acme.transport.linehaul.laneplan.planwindow

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional lane plan budgets by code. */
interface RegionalLanePlanBudgetStore {
    fun find(code: String): RegionalLanePlanBudget?
    fun save(item: RegionalLanePlanBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalLanePlanBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalLanePlanBudgetStore(
    var items: MutableMap<String, RegionalLanePlanBudget>,
    var order: MutableList<String>,
) : RegionalLanePlanBudgetStore {
    override fun find(code: String): RegionalLanePlanBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalLanePlanBudget) {
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

    override fun list(): MutableList<RegionalLanePlanBudget> {
        val out = mutableListOf<RegionalLanePlanBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalLanePlanBudgetStore(): RegionalLanePlanBudgetStore {
    return MemoryRegionalLanePlanBudgetStore(
        mutableMapOf<String, RegionalLanePlanBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalLanePlanBudgetStore(
    var inner: RegionalLanePlanBudgetStore,
    var cache: TtlCache<RegionalLanePlanBudget>,
    var hits: Int,
    var misses: Int,
) : RegionalLanePlanBudgetStore {
    override fun find(code: String): RegionalLanePlanBudget? {
        val hit = (cache.get(code) as RegionalLanePlanBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalLanePlanBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalLanePlanBudget> = inner.list()
}

fun newCachedRegionalLanePlanBudgetStore(inner: RegionalLanePlanBudgetStore, ttl: Int, clock: Clock): RegionalLanePlanBudgetStore {
    return CachedRegionalLanePlanBudgetStore(
        inner = inner,
        cache = newTtlCache<RegionalLanePlanBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional lane plan budgets. Empty fields match all. */
data class RegionalLanePlanBudgetQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalLanePlanBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalLanePlanBudget>): MutableList<RegionalLanePlanBudget> {
        val matched = filter(items) { candidate: RegionalLanePlanBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalLanePlanBudgets(store: RegionalLanePlanBudgetStore, query: RegionalLanePlanBudgetQuery): MutableList<RegionalLanePlanBudget> {
    return query.apply(store.list())
}
