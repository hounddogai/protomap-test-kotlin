package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer budgets by code. */
interface TrailerBudgetStore {
    fun find(code: String): TrailerBudget?
    fun save(item: TrailerBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerBudgetStore(
    var items: MutableMap<String, TrailerBudget>,
    var order: MutableList<String>,
) : TrailerBudgetStore {
    override fun find(code: String): TrailerBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerBudget) {
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

    override fun list(): MutableList<TrailerBudget> {
        val out = mutableListOf<TrailerBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerBudgetStore(): TrailerBudgetStore {
    return MemoryTrailerBudgetStore(mutableMapOf<String, TrailerBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerBudgetStore(
    var inner: TrailerBudgetStore,
    var cache: TtlCache<TrailerBudget>,
    var hits: Int,
    var misses: Int,
) : TrailerBudgetStore {
    override fun find(code: String): TrailerBudget? {
        val hit = (cache.get(code) as TrailerBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerBudget> = inner.list()
}

fun newCachedTrailerBudgetStore(inner: TrailerBudgetStore, ttl: Int, clock: Clock): TrailerBudgetStore {
    return CachedTrailerBudgetStore(
        inner = inner,
        cache = newTtlCache<TrailerBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer budgets. Empty fields match all. */
data class TrailerBudgetQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerBudget>): MutableList<TrailerBudget> {
        val matched = filter(items) { candidate: TrailerBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerBudgets(store: TrailerBudgetStore, query: TrailerBudgetQuery): MutableList<TrailerBudget> {
    return query.apply(store.list())
}
