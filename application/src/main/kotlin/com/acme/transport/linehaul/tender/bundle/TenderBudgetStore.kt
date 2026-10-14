package com.acme.transport.linehaul.tender.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender budgets by code. */
interface TenderBudgetStore {
    fun find(code: String): TenderBudget?
    fun save(item: TenderBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderBudgetStore(
    var items: MutableMap<String, TenderBudget>,
    var order: MutableList<String>,
) : TenderBudgetStore {
    override fun find(code: String): TenderBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderBudget) {
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

    override fun list(): MutableList<TenderBudget> {
        val out = mutableListOf<TenderBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderBudgetStore(): TenderBudgetStore {
    return MemoryTenderBudgetStore(mutableMapOf<String, TenderBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderBudgetStore(
    var inner: TenderBudgetStore,
    var cache: TtlCache<TenderBudget>,
    var hits: Int,
    var misses: Int,
) : TenderBudgetStore {
    override fun find(code: String): TenderBudget? {
        val hit = (cache.get(code) as TenderBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderBudget> = inner.list()
}

fun newCachedTenderBudgetStore(inner: TenderBudgetStore, ttl: Int, clock: Clock): TenderBudgetStore {
    return CachedTenderBudgetStore(
        inner = inner,
        cache = newTtlCache<TenderBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender budgets. Empty fields match all. */
data class TenderBudgetQuery(
    var depot: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderBudget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderBudget>): MutableList<TenderBudget> {
        val matched = filter(items) { candidate: TenderBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderBudgets(store: TenderBudgetStore, query: TenderBudgetQuery): MutableList<TenderBudget> {
    return query.apply(store.list())
}
