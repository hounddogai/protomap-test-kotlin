package com.acme.transport.customs.hold.bundle

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold budgets by code. */
interface HoldBudgetStore {
    fun find(code: String): HoldBudget?
    fun save(item: HoldBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldBudgetStore(
    var items: MutableMap<String, HoldBudget>,
    var order: MutableList<String>,
) : HoldBudgetStore {
    override fun find(code: String): HoldBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldBudget) {
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

    override fun list(): MutableList<HoldBudget> {
        val out = mutableListOf<HoldBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldBudgetStore(): HoldBudgetStore {
    return MemoryHoldBudgetStore(mutableMapOf<String, HoldBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldBudgetStore(
    var inner: HoldBudgetStore,
    var cache: TtlCache<HoldBudget>,
    var hits: Int,
    var misses: Int,
) : HoldBudgetStore {
    override fun find(code: String): HoldBudget? {
        val hit = (cache.get(code) as HoldBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldBudget> = inner.list()
}

fun newCachedHoldBudgetStore(inner: HoldBudgetStore, ttl: Int, clock: Clock): HoldBudgetStore {
    return CachedHoldBudgetStore(
        inner = inner,
        cache = newTtlCache<HoldBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold budgets. Empty fields match all. */
data class HoldBudgetQuery(
    var source: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldBudget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldBudget>): MutableList<HoldBudget> {
        val matched = filter(items) { candidate: HoldBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldBudgets(store: HoldBudgetStore, query: HoldBudgetQuery): MutableList<HoldBudget> {
    return query.apply(store.list())
}
