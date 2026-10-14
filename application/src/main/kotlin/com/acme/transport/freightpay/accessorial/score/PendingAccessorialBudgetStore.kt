package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending accessorial budgets by code. */
interface PendingAccessorialBudgetStore {
    fun find(code: String): PendingAccessorialBudget?
    fun save(item: PendingAccessorialBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAccessorialBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAccessorialBudgetStore(
    var items: MutableMap<String, PendingAccessorialBudget>,
    var order: MutableList<String>,
) : PendingAccessorialBudgetStore {
    override fun find(code: String): PendingAccessorialBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAccessorialBudget) {
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

    override fun list(): MutableList<PendingAccessorialBudget> {
        val out = mutableListOf<PendingAccessorialBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAccessorialBudgetStore(): PendingAccessorialBudgetStore {
    return MemoryPendingAccessorialBudgetStore(
        mutableMapOf<String, PendingAccessorialBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAccessorialBudgetStore(
    var inner: PendingAccessorialBudgetStore,
    var cache: TtlCache<PendingAccessorialBudget>,
    var hits: Int,
    var misses: Int,
) : PendingAccessorialBudgetStore {
    override fun find(code: String): PendingAccessorialBudget? {
        val hit = (cache.get(code) as PendingAccessorialBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAccessorialBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAccessorialBudget> = inner.list()
}

fun newCachedPendingAccessorialBudgetStore(inner: PendingAccessorialBudgetStore, ttl: Int, clock: Clock): PendingAccessorialBudgetStore {
    return CachedPendingAccessorialBudgetStore(
        inner = inner,
        cache = newTtlCache<PendingAccessorialBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending accessorial budgets. Empty fields match all. */
data class PendingAccessorialBudgetQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAccessorialBudget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<PendingAccessorialBudget>): MutableList<PendingAccessorialBudget> {
        val matched = filter(items) { candidate: PendingAccessorialBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAccessorialBudgets(store: PendingAccessorialBudgetStore, query: PendingAccessorialBudgetQuery): MutableList<PendingAccessorialBudget> {
    return query.apply(store.list())
}
