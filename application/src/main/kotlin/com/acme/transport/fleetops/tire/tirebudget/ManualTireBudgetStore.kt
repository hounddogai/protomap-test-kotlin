package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual tire budgets by code. */
interface ManualTireBudgetStore {
    fun find(code: String): ManualTireBudget?
    fun save(item: ManualTireBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTireBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTireBudgetStore(
    var items: MutableMap<String, ManualTireBudget>,
    var order: MutableList<String>,
) : ManualTireBudgetStore {
    override fun find(code: String): ManualTireBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTireBudget) {
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

    override fun list(): MutableList<ManualTireBudget> {
        val out = mutableListOf<ManualTireBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTireBudgetStore(): ManualTireBudgetStore {
    return MemoryManualTireBudgetStore(mutableMapOf<String, ManualTireBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTireBudgetStore(
    var inner: ManualTireBudgetStore,
    var cache: TtlCache<ManualTireBudget>,
    var hits: Int,
    var misses: Int,
) : ManualTireBudgetStore {
    override fun find(code: String): ManualTireBudget? {
        val hit = (cache.get(code) as ManualTireBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTireBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTireBudget> = inner.list()
}

fun newCachedManualTireBudgetStore(inner: ManualTireBudgetStore, ttl: Int, clock: Clock): ManualTireBudgetStore {
    return CachedManualTireBudgetStore(
        inner = inner,
        cache = newTtlCache<ManualTireBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tire budgets. Empty fields match all. */
data class ManualTireBudgetQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTireBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualTireBudget>): MutableList<ManualTireBudget> {
        val matched = filter(items) { candidate: ManualTireBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTireBudgets(store: ManualTireBudgetStore, query: ManualTireBudgetQuery): MutableList<ManualTireBudget> {
    return query.apply(store.list())
}
