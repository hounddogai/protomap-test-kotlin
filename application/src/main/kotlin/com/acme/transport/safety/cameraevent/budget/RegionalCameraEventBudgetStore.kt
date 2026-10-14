package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional camera event budgets by code. */
interface RegionalCameraEventBudgetStore {
    fun find(code: String): RegionalCameraEventBudget?
    fun save(item: RegionalCameraEventBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCameraEventBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCameraEventBudgetStore(
    var items: MutableMap<String, RegionalCameraEventBudget>,
    var order: MutableList<String>,
) : RegionalCameraEventBudgetStore {
    override fun find(code: String): RegionalCameraEventBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCameraEventBudget) {
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

    override fun list(): MutableList<RegionalCameraEventBudget> {
        val out = mutableListOf<RegionalCameraEventBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCameraEventBudgetStore(): RegionalCameraEventBudgetStore {
    return MemoryRegionalCameraEventBudgetStore(
        mutableMapOf<String, RegionalCameraEventBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCameraEventBudgetStore(
    var inner: RegionalCameraEventBudgetStore,
    var cache: TtlCache<RegionalCameraEventBudget>,
    var hits: Int,
    var misses: Int,
) : RegionalCameraEventBudgetStore {
    override fun find(code: String): RegionalCameraEventBudget? {
        val hit = (cache.get(code) as RegionalCameraEventBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCameraEventBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCameraEventBudget> = inner.list()
}

fun newCachedRegionalCameraEventBudgetStore(inner: RegionalCameraEventBudgetStore, ttl: Int, clock: Clock): RegionalCameraEventBudgetStore {
    return CachedRegionalCameraEventBudgetStore(
        inner = inner,
        cache = newTtlCache<RegionalCameraEventBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional camera event budgets. Empty fields match all. */
data class RegionalCameraEventBudgetQuery(
    var channel: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCameraEventBudget): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<RegionalCameraEventBudget>): MutableList<RegionalCameraEventBudget> {
        val matched = filter(items) { candidate: RegionalCameraEventBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCameraEventBudgets(store: RegionalCameraEventBudgetStore, query: RegionalCameraEventBudgetQuery): MutableList<RegionalCameraEventBudget> {
    return query.apply(store.list())
}
