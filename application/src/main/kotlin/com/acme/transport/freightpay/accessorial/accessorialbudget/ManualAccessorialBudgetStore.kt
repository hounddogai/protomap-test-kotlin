package com.acme.transport.freightpay.accessorial.accessorialbudget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual accessorial budgets by code. */
interface ManualAccessorialBudgetStore {
    fun find(code: String): ManualAccessorialBudget?
    fun save(item: ManualAccessorialBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAccessorialBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAccessorialBudgetStore(
    var items: MutableMap<String, ManualAccessorialBudget>,
    var order: MutableList<String>,
) : ManualAccessorialBudgetStore {
    override fun find(code: String): ManualAccessorialBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAccessorialBudget) {
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

    override fun list(): MutableList<ManualAccessorialBudget> {
        val out = mutableListOf<ManualAccessorialBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAccessorialBudgetStore(): ManualAccessorialBudgetStore {
    return MemoryManualAccessorialBudgetStore(
        mutableMapOf<String, ManualAccessorialBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAccessorialBudgetStore(
    var inner: ManualAccessorialBudgetStore,
    var cache: TtlCache<ManualAccessorialBudget>,
    var hits: Int,
    var misses: Int,
) : ManualAccessorialBudgetStore {
    override fun find(code: String): ManualAccessorialBudget? {
        val hit = (cache.get(code) as ManualAccessorialBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAccessorialBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAccessorialBudget> = inner.list()
}

fun newCachedManualAccessorialBudgetStore(inner: ManualAccessorialBudgetStore, ttl: Int, clock: Clock): ManualAccessorialBudgetStore {
    return CachedManualAccessorialBudgetStore(
        inner = inner,
        cache = newTtlCache<ManualAccessorialBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual accessorial budgets. Empty fields match all. */
data class ManualAccessorialBudgetQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAccessorialBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ManualAccessorialBudget>): MutableList<ManualAccessorialBudget> {
        val matched = filter(items) { candidate: ManualAccessorialBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAccessorialBudgets(store: ManualAccessorialBudgetStore, query: ManualAccessorialBudgetQuery): MutableList<ManualAccessorialBudget> {
    return query.apply(store.list())
}
