package com.acme.transport.customs.clearance.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive clearance budgets by code. */
interface AdaptiveClearanceBudgetStore {
    fun find(code: String): AdaptiveClearanceBudget?
    fun save(item: AdaptiveClearanceBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveClearanceBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveClearanceBudgetStore(
    var items: MutableMap<String, AdaptiveClearanceBudget>,
    var order: MutableList<String>,
) : AdaptiveClearanceBudgetStore {
    override fun find(code: String): AdaptiveClearanceBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveClearanceBudget) {
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

    override fun list(): MutableList<AdaptiveClearanceBudget> {
        val out = mutableListOf<AdaptiveClearanceBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveClearanceBudgetStore(): AdaptiveClearanceBudgetStore {
    return MemoryAdaptiveClearanceBudgetStore(
        mutableMapOf<String, AdaptiveClearanceBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveClearanceBudgetStore(
    var inner: AdaptiveClearanceBudgetStore,
    var cache: TtlCache<AdaptiveClearanceBudget>,
    var hits: Int,
    var misses: Int,
) : AdaptiveClearanceBudgetStore {
    override fun find(code: String): AdaptiveClearanceBudget? {
        val hit = (cache.get(code) as AdaptiveClearanceBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveClearanceBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveClearanceBudget> = inner.list()
}

fun newCachedAdaptiveClearanceBudgetStore(inner: AdaptiveClearanceBudgetStore, ttl: Int, clock: Clock): AdaptiveClearanceBudgetStore {
    return CachedAdaptiveClearanceBudgetStore(
        inner = inner,
        cache = newTtlCache<AdaptiveClearanceBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive clearance budgets. Empty fields match all. */
data class AdaptiveClearanceBudgetQuery(
    var lane: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveClearanceBudget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveClearanceBudget>): MutableList<AdaptiveClearanceBudget> {
        val matched = filter(items) { candidate: AdaptiveClearanceBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveClearanceBudgets(store: AdaptiveClearanceBudgetStore, query: AdaptiveClearanceBudgetQuery): MutableList<AdaptiveClearanceBudget> {
    return query.apply(store.list())
}
