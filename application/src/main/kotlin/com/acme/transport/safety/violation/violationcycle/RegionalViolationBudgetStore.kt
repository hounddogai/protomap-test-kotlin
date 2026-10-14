package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional violation budgets by code. */
interface RegionalViolationBudgetStore {
    fun find(code: String): RegionalViolationBudget?
    fun save(item: RegionalViolationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalViolationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalViolationBudgetStore(
    var items: MutableMap<String, RegionalViolationBudget>,
    var order: MutableList<String>,
) : RegionalViolationBudgetStore {
    override fun find(code: String): RegionalViolationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalViolationBudget) {
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

    override fun list(): MutableList<RegionalViolationBudget> {
        val out = mutableListOf<RegionalViolationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalViolationBudgetStore(): RegionalViolationBudgetStore {
    return MemoryRegionalViolationBudgetStore(
        mutableMapOf<String, RegionalViolationBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalViolationBudgetStore(
    var inner: RegionalViolationBudgetStore,
    var cache: TtlCache<RegionalViolationBudget>,
    var hits: Int,
    var misses: Int,
) : RegionalViolationBudgetStore {
    override fun find(code: String): RegionalViolationBudget? {
        val hit = (cache.get(code) as RegionalViolationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalViolationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalViolationBudget> = inner.list()
}

fun newCachedRegionalViolationBudgetStore(inner: RegionalViolationBudgetStore, ttl: Int, clock: Clock): RegionalViolationBudgetStore {
    return CachedRegionalViolationBudgetStore(
        inner = inner,
        cache = newTtlCache<RegionalViolationBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional violation budgets. Empty fields match all. */
data class RegionalViolationBudgetQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalViolationBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalViolationBudget>): MutableList<RegionalViolationBudget> {
        val matched = filter(items) { candidate: RegionalViolationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalViolationBudgets(store: RegionalViolationBudgetStore, query: RegionalViolationBudgetQuery): MutableList<RegionalViolationBudget> {
    return query.apply(store.list())
}
