package com.acme.transport.network.cutoff.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff budgets by code. */
interface CutoffBudgetStore {
    fun find(code: String): CutoffBudget?
    fun save(item: CutoffBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffBudgetStore(
    var items: MutableMap<String, CutoffBudget>,
    var order: MutableList<String>,
) : CutoffBudgetStore {
    override fun find(code: String): CutoffBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffBudget) {
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

    override fun list(): MutableList<CutoffBudget> {
        val out = mutableListOf<CutoffBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffBudgetStore(): CutoffBudgetStore {
    return MemoryCutoffBudgetStore(mutableMapOf<String, CutoffBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffBudgetStore(
    var inner: CutoffBudgetStore,
    var cache: TtlCache<CutoffBudget>,
    var hits: Int,
    var misses: Int,
) : CutoffBudgetStore {
    override fun find(code: String): CutoffBudget? {
        val hit = (cache.get(code) as CutoffBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffBudget> = inner.list()
}

fun newCachedCutoffBudgetStore(inner: CutoffBudgetStore, ttl: Int, clock: Clock): CutoffBudgetStore {
    return CachedCutoffBudgetStore(
        inner = inner,
        cache = newTtlCache<CutoffBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff budgets. Empty fields match all. */
data class CutoffBudgetQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffBudget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffBudget>): MutableList<CutoffBudget> {
        val matched = filter(items) { candidate: CutoffBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffBudgets(store: CutoffBudgetStore, query: CutoffBudgetQuery): MutableList<CutoffBudget> {
    return query.apply(store.list())
}
