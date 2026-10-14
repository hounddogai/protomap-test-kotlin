package com.acme.transport.network.servicearea.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area budgets by code. */
interface ServiceAreaBudgetStore {
    fun find(code: String): ServiceAreaBudget?
    fun save(item: ServiceAreaBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaBudgetStore(
    var items: MutableMap<String, ServiceAreaBudget>,
    var order: MutableList<String>,
) : ServiceAreaBudgetStore {
    override fun find(code: String): ServiceAreaBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaBudget) {
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

    override fun list(): MutableList<ServiceAreaBudget> {
        val out = mutableListOf<ServiceAreaBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaBudgetStore(): ServiceAreaBudgetStore {
    return MemoryServiceAreaBudgetStore(mutableMapOf<String, ServiceAreaBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaBudgetStore(
    var inner: ServiceAreaBudgetStore,
    var cache: TtlCache<ServiceAreaBudget>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaBudgetStore {
    override fun find(code: String): ServiceAreaBudget? {
        val hit = (cache.get(code) as ServiceAreaBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaBudget> = inner.list()
}

fun newCachedServiceAreaBudgetStore(inner: ServiceAreaBudgetStore, ttl: Int, clock: Clock): ServiceAreaBudgetStore {
    return CachedServiceAreaBudgetStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area budgets. Empty fields match all. */
data class ServiceAreaBudgetQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaBudget>): MutableList<ServiceAreaBudget> {
        val matched = filter(items) { candidate: ServiceAreaBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaBudgets(store: ServiceAreaBudgetStore, query: ServiceAreaBudgetQuery): MutableList<ServiceAreaBudget> {
    return query.apply(store.list())
}
