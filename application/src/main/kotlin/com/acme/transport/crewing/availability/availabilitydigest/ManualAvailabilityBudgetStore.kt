package com.acme.transport.crewing.availability.availabilitydigest

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual availability budgets by code. */
interface ManualAvailabilityBudgetStore {
    fun find(code: String): ManualAvailabilityBudget?
    fun save(item: ManualAvailabilityBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAvailabilityBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAvailabilityBudgetStore(
    var items: MutableMap<String, ManualAvailabilityBudget>,
    var order: MutableList<String>,
) : ManualAvailabilityBudgetStore {
    override fun find(code: String): ManualAvailabilityBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAvailabilityBudget) {
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

    override fun list(): MutableList<ManualAvailabilityBudget> {
        val out = mutableListOf<ManualAvailabilityBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAvailabilityBudgetStore(): ManualAvailabilityBudgetStore {
    return MemoryManualAvailabilityBudgetStore(
        mutableMapOf<String, ManualAvailabilityBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAvailabilityBudgetStore(
    var inner: ManualAvailabilityBudgetStore,
    var cache: TtlCache<ManualAvailabilityBudget>,
    var hits: Int,
    var misses: Int,
) : ManualAvailabilityBudgetStore {
    override fun find(code: String): ManualAvailabilityBudget? {
        val hit = (cache.get(code) as ManualAvailabilityBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAvailabilityBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAvailabilityBudget> = inner.list()
}

fun newCachedManualAvailabilityBudgetStore(inner: ManualAvailabilityBudgetStore, ttl: Int, clock: Clock): ManualAvailabilityBudgetStore {
    return CachedManualAvailabilityBudgetStore(
        inner = inner,
        cache = newTtlCache<ManualAvailabilityBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual availability budgets. Empty fields match all. */
data class ManualAvailabilityBudgetQuery(
    var category: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAvailabilityBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAvailabilityBudget>): MutableList<ManualAvailabilityBudget> {
        val matched = filter(items) { candidate: ManualAvailabilityBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAvailabilityBudgets(store: ManualAvailabilityBudgetStore, query: ManualAvailabilityBudgetQuery): MutableList<ManualAvailabilityBudget> {
    return query.apply(store.list())
}
