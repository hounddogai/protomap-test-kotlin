package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling duty period budgets by code. */
interface RollingDutyPeriodBudgetStore {
    fun find(code: String): RollingDutyPeriodBudget?
    fun save(item: RollingDutyPeriodBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDutyPeriodBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDutyPeriodBudgetStore(
    var items: MutableMap<String, RollingDutyPeriodBudget>,
    var order: MutableList<String>,
) : RollingDutyPeriodBudgetStore {
    override fun find(code: String): RollingDutyPeriodBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDutyPeriodBudget) {
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

    override fun list(): MutableList<RollingDutyPeriodBudget> {
        val out = mutableListOf<RollingDutyPeriodBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDutyPeriodBudgetStore(): RollingDutyPeriodBudgetStore {
    return MemoryRollingDutyPeriodBudgetStore(
        mutableMapOf<String, RollingDutyPeriodBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDutyPeriodBudgetStore(
    var inner: RollingDutyPeriodBudgetStore,
    var cache: TtlCache<RollingDutyPeriodBudget>,
    var hits: Int,
    var misses: Int,
) : RollingDutyPeriodBudgetStore {
    override fun find(code: String): RollingDutyPeriodBudget? {
        val hit = (cache.get(code) as RollingDutyPeriodBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDutyPeriodBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDutyPeriodBudget> = inner.list()
}

fun newCachedRollingDutyPeriodBudgetStore(inner: RollingDutyPeriodBudgetStore, ttl: Int, clock: Clock): RollingDutyPeriodBudgetStore {
    return CachedRollingDutyPeriodBudgetStore(
        inner = inner,
        cache = newTtlCache<RollingDutyPeriodBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling duty period budgets. Empty fields match all. */
data class RollingDutyPeriodBudgetQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDutyPeriodBudget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDutyPeriodBudget>): MutableList<RollingDutyPeriodBudget> {
        val matched = filter(items) { candidate: RollingDutyPeriodBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDutyPeriodBudgets(store: RollingDutyPeriodBudgetStore, query: RollingDutyPeriodBudgetQuery): MutableList<RollingDutyPeriodBudget> {
    return query.apply(store.list())
}
