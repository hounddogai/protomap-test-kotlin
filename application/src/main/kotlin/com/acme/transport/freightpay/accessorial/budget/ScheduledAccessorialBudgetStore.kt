package com.acme.transport.freightpay.accessorial.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled accessorial budgets by code. */
interface ScheduledAccessorialBudgetStore {
    fun find(code: String): ScheduledAccessorialBudget?
    fun save(item: ScheduledAccessorialBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAccessorialBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAccessorialBudgetStore(
    var items: MutableMap<String, ScheduledAccessorialBudget>,
    var order: MutableList<String>,
) : ScheduledAccessorialBudgetStore {
    override fun find(code: String): ScheduledAccessorialBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAccessorialBudget) {
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

    override fun list(): MutableList<ScheduledAccessorialBudget> {
        val out = mutableListOf<ScheduledAccessorialBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAccessorialBudgetStore(): ScheduledAccessorialBudgetStore {
    return MemoryScheduledAccessorialBudgetStore(
        mutableMapOf<String, ScheduledAccessorialBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAccessorialBudgetStore(
    var inner: ScheduledAccessorialBudgetStore,
    var cache: TtlCache<ScheduledAccessorialBudget>,
    var hits: Int,
    var misses: Int,
) : ScheduledAccessorialBudgetStore {
    override fun find(code: String): ScheduledAccessorialBudget? {
        val hit = (cache.get(code) as ScheduledAccessorialBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAccessorialBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAccessorialBudget> = inner.list()
}

fun newCachedScheduledAccessorialBudgetStore(inner: ScheduledAccessorialBudgetStore, ttl: Int, clock: Clock): ScheduledAccessorialBudgetStore {
    return CachedScheduledAccessorialBudgetStore(
        inner = inner,
        cache = newTtlCache<ScheduledAccessorialBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled accessorial budgets. Empty fields match all. */
data class ScheduledAccessorialBudgetQuery(
    var country: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAccessorialBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAccessorialBudget>): MutableList<ScheduledAccessorialBudget> {
        val matched = filter(items) { candidate: ScheduledAccessorialBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAccessorialBudgets(store: ScheduledAccessorialBudgetStore, query: ScheduledAccessorialBudgetQuery): MutableList<ScheduledAccessorialBudget> {
    return query.apply(store.list())
}
