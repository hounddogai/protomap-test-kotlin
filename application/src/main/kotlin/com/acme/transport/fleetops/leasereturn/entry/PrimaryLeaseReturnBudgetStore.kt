package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary lease return budgets by code. */
interface PrimaryLeaseReturnBudgetStore {
    fun find(code: String): PrimaryLeaseReturnBudget?
    fun save(item: PrimaryLeaseReturnBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryLeaseReturnBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryLeaseReturnBudgetStore(
    var items: MutableMap<String, PrimaryLeaseReturnBudget>,
    var order: MutableList<String>,
) : PrimaryLeaseReturnBudgetStore {
    override fun find(code: String): PrimaryLeaseReturnBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryLeaseReturnBudget) {
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

    override fun list(): MutableList<PrimaryLeaseReturnBudget> {
        val out = mutableListOf<PrimaryLeaseReturnBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryLeaseReturnBudgetStore(): PrimaryLeaseReturnBudgetStore {
    return MemoryPrimaryLeaseReturnBudgetStore(
        mutableMapOf<String, PrimaryLeaseReturnBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryLeaseReturnBudgetStore(
    var inner: PrimaryLeaseReturnBudgetStore,
    var cache: TtlCache<PrimaryLeaseReturnBudget>,
    var hits: Int,
    var misses: Int,
) : PrimaryLeaseReturnBudgetStore {
    override fun find(code: String): PrimaryLeaseReturnBudget? {
        val hit = (cache.get(code) as PrimaryLeaseReturnBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryLeaseReturnBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryLeaseReturnBudget> = inner.list()
}

fun newCachedPrimaryLeaseReturnBudgetStore(inner: PrimaryLeaseReturnBudgetStore, ttl: Int, clock: Clock): PrimaryLeaseReturnBudgetStore {
    return CachedPrimaryLeaseReturnBudgetStore(
        inner = inner,
        cache = newTtlCache<PrimaryLeaseReturnBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary lease return budgets. Empty fields match all. */
data class PrimaryLeaseReturnBudgetQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryLeaseReturnBudget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryLeaseReturnBudget>): MutableList<PrimaryLeaseReturnBudget> {
        val matched = filter(items) { candidate: PrimaryLeaseReturnBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryLeaseReturnBudgets(store: PrimaryLeaseReturnBudgetStore, query: PrimaryLeaseReturnBudgetQuery): MutableList<PrimaryLeaseReturnBudget> {
    return query.apply(store.list())
}
