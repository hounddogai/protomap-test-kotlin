package com.acme.transport.crewing.availability.forecast

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability budgets by code. */
interface AvailabilityBudgetStore {
    fun find(code: String): AvailabilityBudget?
    fun save(item: AvailabilityBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityBudgetStore(
    var items: MutableMap<String, AvailabilityBudget>,
    var order: MutableList<String>,
) : AvailabilityBudgetStore {
    override fun find(code: String): AvailabilityBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityBudget) {
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

    override fun list(): MutableList<AvailabilityBudget> {
        val out = mutableListOf<AvailabilityBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityBudgetStore(): AvailabilityBudgetStore {
    return MemoryAvailabilityBudgetStore(mutableMapOf<String, AvailabilityBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityBudgetStore(
    var inner: AvailabilityBudgetStore,
    var cache: TtlCache<AvailabilityBudget>,
    var hits: Int,
    var misses: Int,
) : AvailabilityBudgetStore {
    override fun find(code: String): AvailabilityBudget? {
        val hit = (cache.get(code) as AvailabilityBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityBudget> = inner.list()
}

fun newCachedAvailabilityBudgetStore(inner: AvailabilityBudgetStore, ttl: Int, clock: Clock): AvailabilityBudgetStore {
    return CachedAvailabilityBudgetStore(
        inner = inner,
        cache = newTtlCache<AvailabilityBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability budgets. Empty fields match all. */
data class AvailabilityBudgetQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityBudget>): MutableList<AvailabilityBudget> {
        val matched = filter(items) { candidate: AvailabilityBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityBudgets(store: AvailabilityBudgetStore, query: AvailabilityBudgetQuery): MutableList<AvailabilityBudget> {
    return query.apply(store.list())
}
