package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge budgets by code. */
interface EvChargeBudgetStore {
    fun find(code: String): EvChargeBudget?
    fun save(item: EvChargeBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeBudgetStore(
    var items: MutableMap<String, EvChargeBudget>,
    var order: MutableList<String>,
) : EvChargeBudgetStore {
    override fun find(code: String): EvChargeBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeBudget) {
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

    override fun list(): MutableList<EvChargeBudget> {
        val out = mutableListOf<EvChargeBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeBudgetStore(): EvChargeBudgetStore {
    return MemoryEvChargeBudgetStore(mutableMapOf<String, EvChargeBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeBudgetStore(
    var inner: EvChargeBudgetStore,
    var cache: TtlCache<EvChargeBudget>,
    var hits: Int,
    var misses: Int,
) : EvChargeBudgetStore {
    override fun find(code: String): EvChargeBudget? {
        val hit = (cache.get(code) as EvChargeBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeBudget> = inner.list()
}

fun newCachedEvChargeBudgetStore(inner: EvChargeBudgetStore, ttl: Int, clock: Clock): EvChargeBudgetStore {
    return CachedEvChargeBudgetStore(
        inner = inner,
        cache = newTtlCache<EvChargeBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge budgets. Empty fields match all. */
data class EvChargeBudgetQuery(
    var region: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeBudget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<EvChargeBudget>): MutableList<EvChargeBudget> {
        val matched = filter(items) { candidate: EvChargeBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeBudgets(store: EvChargeBudgetStore, query: EvChargeBudgetQuery): MutableList<EvChargeBudget> {
    return query.apply(store.list())
}
