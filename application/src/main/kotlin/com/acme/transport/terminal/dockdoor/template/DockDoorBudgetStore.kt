package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door budgets by code. */
interface DockDoorBudgetStore {
    fun find(code: String): DockDoorBudget?
    fun save(item: DockDoorBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorBudgetStore(
    var items: MutableMap<String, DockDoorBudget>,
    var order: MutableList<String>,
) : DockDoorBudgetStore {
    override fun find(code: String): DockDoorBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorBudget) {
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

    override fun list(): MutableList<DockDoorBudget> {
        val out = mutableListOf<DockDoorBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorBudgetStore(): DockDoorBudgetStore {
    return MemoryDockDoorBudgetStore(mutableMapOf<String, DockDoorBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorBudgetStore(
    var inner: DockDoorBudgetStore,
    var cache: TtlCache<DockDoorBudget>,
    var hits: Int,
    var misses: Int,
) : DockDoorBudgetStore {
    override fun find(code: String): DockDoorBudget? {
        val hit = (cache.get(code) as DockDoorBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorBudget> = inner.list()
}

fun newCachedDockDoorBudgetStore(inner: DockDoorBudgetStore, ttl: Int, clock: Clock): DockDoorBudgetStore {
    return CachedDockDoorBudgetStore(
        inner = inner,
        cache = newTtlCache<DockDoorBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door budgets. Empty fields match all. */
data class DockDoorBudgetQuery(
    var locale: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorBudget>): MutableList<DockDoorBudget> {
        val matched = filter(items) { candidate: DockDoorBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorBudgets(store: DockDoorBudgetStore, query: DockDoorBudgetQuery): MutableList<DockDoorBudget> {
    return query.apply(store.list())
}
