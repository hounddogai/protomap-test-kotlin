package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period budgets by code. */
interface DutyPeriodBudgetStore {
    fun find(code: String): DutyPeriodBudget?
    fun save(item: DutyPeriodBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodBudgetStore(
    var items: MutableMap<String, DutyPeriodBudget>,
    var order: MutableList<String>,
) : DutyPeriodBudgetStore {
    override fun find(code: String): DutyPeriodBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodBudget) {
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

    override fun list(): MutableList<DutyPeriodBudget> {
        val out = mutableListOf<DutyPeriodBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodBudgetStore(): DutyPeriodBudgetStore {
    return MemoryDutyPeriodBudgetStore(mutableMapOf<String, DutyPeriodBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodBudgetStore(
    var inner: DutyPeriodBudgetStore,
    var cache: TtlCache<DutyPeriodBudget>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodBudgetStore {
    override fun find(code: String): DutyPeriodBudget? {
        val hit = (cache.get(code) as DutyPeriodBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodBudget> = inner.list()
}

fun newCachedDutyPeriodBudgetStore(inner: DutyPeriodBudgetStore, ttl: Int, clock: Clock): DutyPeriodBudgetStore {
    return CachedDutyPeriodBudgetStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period budgets. Empty fields match all. */
data class DutyPeriodBudgetQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DutyPeriodBudget>): MutableList<DutyPeriodBudget> {
        val matched = filter(items) { candidate: DutyPeriodBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodBudgets(store: DutyPeriodBudgetStore, query: DutyPeriodBudgetQuery): MutableList<DutyPeriodBudget> {
    return query.apply(store.list())
}
