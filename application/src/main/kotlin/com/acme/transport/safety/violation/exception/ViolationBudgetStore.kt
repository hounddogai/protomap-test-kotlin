package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation budgets by code. */
interface ViolationBudgetStore {
    fun find(code: String): ViolationBudget?
    fun save(item: ViolationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationBudgetStore(
    var items: MutableMap<String, ViolationBudget>,
    var order: MutableList<String>,
) : ViolationBudgetStore {
    override fun find(code: String): ViolationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationBudget) {
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

    override fun list(): MutableList<ViolationBudget> {
        val out = mutableListOf<ViolationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationBudgetStore(): ViolationBudgetStore {
    return MemoryViolationBudgetStore(mutableMapOf<String, ViolationBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationBudgetStore(
    var inner: ViolationBudgetStore,
    var cache: TtlCache<ViolationBudget>,
    var hits: Int,
    var misses: Int,
) : ViolationBudgetStore {
    override fun find(code: String): ViolationBudget? {
        val hit = (cache.get(code) as ViolationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationBudget> = inner.list()
}

fun newCachedViolationBudgetStore(inner: ViolationBudgetStore, ttl: Int, clock: Clock): ViolationBudgetStore {
    return CachedViolationBudgetStore(
        inner = inner,
        cache = newTtlCache<ViolationBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation budgets. Empty fields match all. */
data class ViolationBudgetQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationBudget>): MutableList<ViolationBudget> {
        val matched = filter(items) { candidate: ViolationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationBudgets(store: ViolationBudgetStore, query: ViolationBudgetQuery): MutableList<ViolationBudget> {
    return query.apply(store.list())
}
