package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return budgets by code. */
interface LeaseReturnBudgetStore {
    fun find(code: String): LeaseReturnBudget?
    fun save(item: LeaseReturnBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnBudgetStore(
    var items: MutableMap<String, LeaseReturnBudget>,
    var order: MutableList<String>,
) : LeaseReturnBudgetStore {
    override fun find(code: String): LeaseReturnBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnBudget) {
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

    override fun list(): MutableList<LeaseReturnBudget> {
        val out = mutableListOf<LeaseReturnBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnBudgetStore(): LeaseReturnBudgetStore {
    return MemoryLeaseReturnBudgetStore(mutableMapOf<String, LeaseReturnBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnBudgetStore(
    var inner: LeaseReturnBudgetStore,
    var cache: TtlCache<LeaseReturnBudget>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnBudgetStore {
    override fun find(code: String): LeaseReturnBudget? {
        val hit = (cache.get(code) as LeaseReturnBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnBudget> = inner.list()
}

fun newCachedLeaseReturnBudgetStore(inner: LeaseReturnBudgetStore, ttl: Int, clock: Clock): LeaseReturnBudgetStore {
    return CachedLeaseReturnBudgetStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return budgets. Empty fields match all. */
data class LeaseReturnBudgetQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnBudget>): MutableList<LeaseReturnBudget> {
        val matched = filter(items) { candidate: LeaseReturnBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnBudgets(store: LeaseReturnBudgetStore, query: LeaseReturnBudgetQuery): MutableList<LeaseReturnBudget> {
    return query.apply(store.list())
}
