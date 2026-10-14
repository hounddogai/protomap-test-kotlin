package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled locker budgets by code. */
interface ScheduledLockerBudgetStore {
    fun find(code: String): ScheduledLockerBudget?
    fun save(item: ScheduledLockerBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledLockerBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledLockerBudgetStore(
    var items: MutableMap<String, ScheduledLockerBudget>,
    var order: MutableList<String>,
) : ScheduledLockerBudgetStore {
    override fun find(code: String): ScheduledLockerBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledLockerBudget) {
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

    override fun list(): MutableList<ScheduledLockerBudget> {
        val out = mutableListOf<ScheduledLockerBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledLockerBudgetStore(): ScheduledLockerBudgetStore {
    return MemoryScheduledLockerBudgetStore(
        mutableMapOf<String, ScheduledLockerBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledLockerBudgetStore(
    var inner: ScheduledLockerBudgetStore,
    var cache: TtlCache<ScheduledLockerBudget>,
    var hits: Int,
    var misses: Int,
) : ScheduledLockerBudgetStore {
    override fun find(code: String): ScheduledLockerBudget? {
        val hit = (cache.get(code) as ScheduledLockerBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledLockerBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledLockerBudget> = inner.list()
}

fun newCachedScheduledLockerBudgetStore(inner: ScheduledLockerBudgetStore, ttl: Int, clock: Clock): ScheduledLockerBudgetStore {
    return CachedScheduledLockerBudgetStore(
        inner = inner,
        cache = newTtlCache<ScheduledLockerBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled locker budgets. Empty fields match all. */
data class ScheduledLockerBudgetQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledLockerBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledLockerBudget>): MutableList<ScheduledLockerBudget> {
        val matched = filter(items) { candidate: ScheduledLockerBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledLockerBudgets(store: ScheduledLockerBudgetStore, query: ScheduledLockerBudgetQuery): MutableList<ScheduledLockerBudget> {
    return query.apply(store.list())
}
