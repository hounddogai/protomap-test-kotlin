package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending duty period budgets by code. */
interface PendingDutyPeriodBudgetStore {
    fun find(code: String): PendingDutyPeriodBudget?
    fun save(item: PendingDutyPeriodBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDutyPeriodBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDutyPeriodBudgetStore(
    var items: MutableMap<String, PendingDutyPeriodBudget>,
    var order: MutableList<String>,
) : PendingDutyPeriodBudgetStore {
    override fun find(code: String): PendingDutyPeriodBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDutyPeriodBudget) {
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

    override fun list(): MutableList<PendingDutyPeriodBudget> {
        val out = mutableListOf<PendingDutyPeriodBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDutyPeriodBudgetStore(): PendingDutyPeriodBudgetStore {
    return MemoryPendingDutyPeriodBudgetStore(
        mutableMapOf<String, PendingDutyPeriodBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDutyPeriodBudgetStore(
    var inner: PendingDutyPeriodBudgetStore,
    var cache: TtlCache<PendingDutyPeriodBudget>,
    var hits: Int,
    var misses: Int,
) : PendingDutyPeriodBudgetStore {
    override fun find(code: String): PendingDutyPeriodBudget? {
        val hit = (cache.get(code) as PendingDutyPeriodBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDutyPeriodBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDutyPeriodBudget> = inner.list()
}

fun newCachedPendingDutyPeriodBudgetStore(inner: PendingDutyPeriodBudgetStore, ttl: Int, clock: Clock): PendingDutyPeriodBudgetStore {
    return CachedPendingDutyPeriodBudgetStore(
        inner = inner,
        cache = newTtlCache<PendingDutyPeriodBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending duty period budgets. Empty fields match all. */
data class PendingDutyPeriodBudgetQuery(
    var segment: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDutyPeriodBudget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDutyPeriodBudget>): MutableList<PendingDutyPeriodBudget> {
        val matched = filter(items) { candidate: PendingDutyPeriodBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDutyPeriodBudgets(store: PendingDutyPeriodBudgetStore, query: PendingDutyPeriodBudgetQuery): MutableList<PendingDutyPeriodBudget> {
    return query.apply(store.list())
}
