package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending tender budgets by code. */
interface PendingTenderBudgetStore {
    fun find(code: String): PendingTenderBudget?
    fun save(item: PendingTenderBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTenderBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTenderBudgetStore(
    var items: MutableMap<String, PendingTenderBudget>,
    var order: MutableList<String>,
) : PendingTenderBudgetStore {
    override fun find(code: String): PendingTenderBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTenderBudget) {
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

    override fun list(): MutableList<PendingTenderBudget> {
        val out = mutableListOf<PendingTenderBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTenderBudgetStore(): PendingTenderBudgetStore {
    return MemoryPendingTenderBudgetStore(mutableMapOf<String, PendingTenderBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTenderBudgetStore(
    var inner: PendingTenderBudgetStore,
    var cache: TtlCache<PendingTenderBudget>,
    var hits: Int,
    var misses: Int,
) : PendingTenderBudgetStore {
    override fun find(code: String): PendingTenderBudget? {
        val hit = (cache.get(code) as PendingTenderBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTenderBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTenderBudget> = inner.list()
}

fun newCachedPendingTenderBudgetStore(inner: PendingTenderBudgetStore, ttl: Int, clock: Clock): PendingTenderBudgetStore {
    return CachedPendingTenderBudgetStore(
        inner = inner,
        cache = newTtlCache<PendingTenderBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tender budgets. Empty fields match all. */
data class PendingTenderBudgetQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTenderBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTenderBudget>): MutableList<PendingTenderBudget> {
        val matched = filter(items) { candidate: PendingTenderBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTenderBudgets(store: PendingTenderBudgetStore, query: PendingTenderBudgetQuery): MutableList<PendingTenderBudget> {
    return query.apply(store.list())
}
