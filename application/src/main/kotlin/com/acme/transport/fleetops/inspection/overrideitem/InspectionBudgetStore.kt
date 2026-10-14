package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection budgets by code. */
interface InspectionBudgetStore {
    fun find(code: String): InspectionBudget?
    fun save(item: InspectionBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionBudgetStore(
    var items: MutableMap<String, InspectionBudget>,
    var order: MutableList<String>,
) : InspectionBudgetStore {
    override fun find(code: String): InspectionBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionBudget) {
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

    override fun list(): MutableList<InspectionBudget> {
        val out = mutableListOf<InspectionBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionBudgetStore(): InspectionBudgetStore {
    return MemoryInspectionBudgetStore(mutableMapOf<String, InspectionBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionBudgetStore(
    var inner: InspectionBudgetStore,
    var cache: TtlCache<InspectionBudget>,
    var hits: Int,
    var misses: Int,
) : InspectionBudgetStore {
    override fun find(code: String): InspectionBudget? {
        val hit = (cache.get(code) as InspectionBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionBudget> = inner.list()
}

fun newCachedInspectionBudgetStore(inner: InspectionBudgetStore, ttl: Int, clock: Clock): InspectionBudgetStore {
    return CachedInspectionBudgetStore(
        inner = inner,
        cache = newTtlCache<InspectionBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection budgets. Empty fields match all. */
data class InspectionBudgetQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionBudget>): MutableList<InspectionBudget> {
        val matched = filter(items) { candidate: InspectionBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionBudgets(store: InspectionBudgetStore, query: InspectionBudgetQuery): MutableList<InspectionBudget> {
    return query.apply(store.list())
}
