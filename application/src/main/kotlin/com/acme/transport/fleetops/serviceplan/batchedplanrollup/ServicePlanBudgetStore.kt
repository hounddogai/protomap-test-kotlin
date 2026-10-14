package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan budgets by code. */
interface ServicePlanBudgetStore {
    fun find(code: String): ServicePlanBudget?
    fun save(item: ServicePlanBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanBudgetStore(
    var items: MutableMap<String, ServicePlanBudget>,
    var order: MutableList<String>,
) : ServicePlanBudgetStore {
    override fun find(code: String): ServicePlanBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanBudget) {
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

    override fun list(): MutableList<ServicePlanBudget> {
        val out = mutableListOf<ServicePlanBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanBudgetStore(): ServicePlanBudgetStore {
    return MemoryServicePlanBudgetStore(mutableMapOf<String, ServicePlanBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanBudgetStore(
    var inner: ServicePlanBudgetStore,
    var cache: TtlCache<ServicePlanBudget>,
    var hits: Int,
    var misses: Int,
) : ServicePlanBudgetStore {
    override fun find(code: String): ServicePlanBudget? {
        val hit = (cache.get(code) as ServicePlanBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanBudget> = inner.list()
}

fun newCachedServicePlanBudgetStore(inner: ServicePlanBudgetStore, ttl: Int, clock: Clock): ServicePlanBudgetStore {
    return CachedServicePlanBudgetStore(
        inner = inner,
        cache = newTtlCache<ServicePlanBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan budgets. Empty fields match all. */
data class ServicePlanBudgetQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanBudget>): MutableList<ServicePlanBudget> {
        val matched = filter(items) { candidate: ServicePlanBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanBudgets(store: ServicePlanBudgetStore, query: ServicePlanBudgetQuery): MutableList<ServicePlanBudget> {
    return query.apply(store.list())
}
