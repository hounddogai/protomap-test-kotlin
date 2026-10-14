package com.acme.transport.safety.violation.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global violation budgets by code. */
interface GlobalViolationBudgetStore {
    fun find(code: String): GlobalViolationBudget?
    fun save(item: GlobalViolationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalViolationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalViolationBudgetStore(
    var items: MutableMap<String, GlobalViolationBudget>,
    var order: MutableList<String>,
) : GlobalViolationBudgetStore {
    override fun find(code: String): GlobalViolationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalViolationBudget) {
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

    override fun list(): MutableList<GlobalViolationBudget> {
        val out = mutableListOf<GlobalViolationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalViolationBudgetStore(): GlobalViolationBudgetStore {
    return MemoryGlobalViolationBudgetStore(
        mutableMapOf<String, GlobalViolationBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalViolationBudgetStore(
    var inner: GlobalViolationBudgetStore,
    var cache: TtlCache<GlobalViolationBudget>,
    var hits: Int,
    var misses: Int,
) : GlobalViolationBudgetStore {
    override fun find(code: String): GlobalViolationBudget? {
        val hit = (cache.get(code) as GlobalViolationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalViolationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalViolationBudget> = inner.list()
}

fun newCachedGlobalViolationBudgetStore(inner: GlobalViolationBudgetStore, ttl: Int, clock: Clock): GlobalViolationBudgetStore {
    return CachedGlobalViolationBudgetStore(
        inner = inner,
        cache = newTtlCache<GlobalViolationBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global violation budgets. Empty fields match all. */
data class GlobalViolationBudgetQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalViolationBudget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<GlobalViolationBudget>): MutableList<GlobalViolationBudget> {
        val matched = filter(items) { candidate: GlobalViolationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalViolationBudgets(store: GlobalViolationBudgetStore, query: GlobalViolationBudgetQuery): MutableList<GlobalViolationBudget> {
    return query.apply(store.list())
}
