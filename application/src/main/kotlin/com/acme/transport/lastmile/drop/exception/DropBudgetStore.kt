package com.acme.transport.lastmile.drop.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop budgets by code. */
interface DropBudgetStore {
    fun find(code: String): DropBudget?
    fun save(item: DropBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropBudgetStore(
    var items: MutableMap<String, DropBudget>,
    var order: MutableList<String>,
) : DropBudgetStore {
    override fun find(code: String): DropBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropBudget) {
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

    override fun list(): MutableList<DropBudget> {
        val out = mutableListOf<DropBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropBudgetStore(): DropBudgetStore {
    return MemoryDropBudgetStore(mutableMapOf<String, DropBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropBudgetStore(
    var inner: DropBudgetStore,
    var cache: TtlCache<DropBudget>,
    var hits: Int,
    var misses: Int,
) : DropBudgetStore {
    override fun find(code: String): DropBudget? {
        val hit = (cache.get(code) as DropBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropBudget> = inner.list()
}

fun newCachedDropBudgetStore(inner: DropBudgetStore, ttl: Int, clock: Clock): DropBudgetStore {
    return CachedDropBudgetStore(
        inner = inner,
        cache = newTtlCache<DropBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop budgets. Empty fields match all. */
data class DropBudgetQuery(
    var country: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropBudget>): MutableList<DropBudget> {
        val matched = filter(items) { candidate: DropBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropBudgets(store: DropBudgetStore, query: DropBudgetQuery): MutableList<DropBudget> {
    return query.apply(store.list())
}
