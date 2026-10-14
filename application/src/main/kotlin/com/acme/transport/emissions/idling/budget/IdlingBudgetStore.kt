package com.acme.transport.emissions.idling.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling budgets by code. */
interface IdlingBudgetStore {
    fun find(code: String): IdlingBudget?
    fun save(item: IdlingBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingBudgetStore(
    var items: MutableMap<String, IdlingBudget>,
    var order: MutableList<String>,
) : IdlingBudgetStore {
    override fun find(code: String): IdlingBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingBudget) {
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

    override fun list(): MutableList<IdlingBudget> {
        val out = mutableListOf<IdlingBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingBudgetStore(): IdlingBudgetStore {
    return MemoryIdlingBudgetStore(mutableMapOf<String, IdlingBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingBudgetStore(
    var inner: IdlingBudgetStore,
    var cache: TtlCache<IdlingBudget>,
    var hits: Int,
    var misses: Int,
) : IdlingBudgetStore {
    override fun find(code: String): IdlingBudget? {
        val hit = (cache.get(code) as IdlingBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingBudget> = inner.list()
}

fun newCachedIdlingBudgetStore(inner: IdlingBudgetStore, ttl: Int, clock: Clock): IdlingBudgetStore {
    return CachedIdlingBudgetStore(
        inner = inner,
        cache = newTtlCache<IdlingBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling budgets. Empty fields match all. */
data class IdlingBudgetQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingBudget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingBudget>): MutableList<IdlingBudget> {
        val matched = filter(items) { candidate: IdlingBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingBudgets(store: IdlingBudgetStore, query: IdlingBudgetQuery): MutableList<IdlingBudget> {
    return query.apply(store.list())
}
