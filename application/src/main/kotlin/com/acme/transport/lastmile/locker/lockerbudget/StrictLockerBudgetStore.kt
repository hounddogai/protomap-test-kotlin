package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict locker budgets by code. */
interface StrictLockerBudgetStore {
    fun find(code: String): StrictLockerBudget?
    fun save(item: StrictLockerBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLockerBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLockerBudgetStore(
    var items: MutableMap<String, StrictLockerBudget>,
    var order: MutableList<String>,
) : StrictLockerBudgetStore {
    override fun find(code: String): StrictLockerBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLockerBudget) {
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

    override fun list(): MutableList<StrictLockerBudget> {
        val out = mutableListOf<StrictLockerBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLockerBudgetStore(): StrictLockerBudgetStore {
    return MemoryStrictLockerBudgetStore(mutableMapOf<String, StrictLockerBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLockerBudgetStore(
    var inner: StrictLockerBudgetStore,
    var cache: TtlCache<StrictLockerBudget>,
    var hits: Int,
    var misses: Int,
) : StrictLockerBudgetStore {
    override fun find(code: String): StrictLockerBudget? {
        val hit = (cache.get(code) as StrictLockerBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLockerBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLockerBudget> = inner.list()
}

fun newCachedStrictLockerBudgetStore(inner: StrictLockerBudgetStore, ttl: Int, clock: Clock): StrictLockerBudgetStore {
    return CachedStrictLockerBudgetStore(
        inner = inner,
        cache = newTtlCache<StrictLockerBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict locker budgets. Empty fields match all. */
data class StrictLockerBudgetQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLockerBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLockerBudget>): MutableList<StrictLockerBudget> {
        val matched = filter(items) { candidate: StrictLockerBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLockerBudgets(store: StrictLockerBudgetStore, query: StrictLockerBudgetQuery): MutableList<StrictLockerBudget> {
    return query.apply(store.list())
}
