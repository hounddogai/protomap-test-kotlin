package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect budgets by code. */
interface DefectBudgetStore {
    fun find(code: String): DefectBudget?
    fun save(item: DefectBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectBudgetStore(
    var items: MutableMap<String, DefectBudget>,
    var order: MutableList<String>,
) : DefectBudgetStore {
    override fun find(code: String): DefectBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectBudget) {
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

    override fun list(): MutableList<DefectBudget> {
        val out = mutableListOf<DefectBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectBudgetStore(): DefectBudgetStore {
    return MemoryDefectBudgetStore(mutableMapOf<String, DefectBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectBudgetStore(
    var inner: DefectBudgetStore,
    var cache: TtlCache<DefectBudget>,
    var hits: Int,
    var misses: Int,
) : DefectBudgetStore {
    override fun find(code: String): DefectBudget? {
        val hit = (cache.get(code) as DefectBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectBudget> = inner.list()
}

fun newCachedDefectBudgetStore(inner: DefectBudgetStore, ttl: Int, clock: Clock): DefectBudgetStore {
    return CachedDefectBudgetStore(
        inner = inner,
        cache = newTtlCache<DefectBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect budgets. Empty fields match all. */
data class DefectBudgetQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectBudget>): MutableList<DefectBudget> {
        val matched = filter(items) { candidate: DefectBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectBudgets(store: DefectBudgetStore, query: DefectBudgetQuery): MutableList<DefectBudget> {
    return query.apply(store.list())
}
