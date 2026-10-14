package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global camera event budgets by code. */
interface GlobalCameraEventBudgetStore {
    fun find(code: String): GlobalCameraEventBudget?
    fun save(item: GlobalCameraEventBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCameraEventBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCameraEventBudgetStore(
    var items: MutableMap<String, GlobalCameraEventBudget>,
    var order: MutableList<String>,
) : GlobalCameraEventBudgetStore {
    override fun find(code: String): GlobalCameraEventBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCameraEventBudget) {
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

    override fun list(): MutableList<GlobalCameraEventBudget> {
        val out = mutableListOf<GlobalCameraEventBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCameraEventBudgetStore(): GlobalCameraEventBudgetStore {
    return MemoryGlobalCameraEventBudgetStore(
        mutableMapOf<String, GlobalCameraEventBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCameraEventBudgetStore(
    var inner: GlobalCameraEventBudgetStore,
    var cache: TtlCache<GlobalCameraEventBudget>,
    var hits: Int,
    var misses: Int,
) : GlobalCameraEventBudgetStore {
    override fun find(code: String): GlobalCameraEventBudget? {
        val hit = (cache.get(code) as GlobalCameraEventBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCameraEventBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCameraEventBudget> = inner.list()
}

fun newCachedGlobalCameraEventBudgetStore(inner: GlobalCameraEventBudgetStore, ttl: Int, clock: Clock): GlobalCameraEventBudgetStore {
    return CachedGlobalCameraEventBudgetStore(
        inner = inner,
        cache = newTtlCache<GlobalCameraEventBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global camera event budgets. Empty fields match all. */
data class GlobalCameraEventBudgetQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCameraEventBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCameraEventBudget>): MutableList<GlobalCameraEventBudget> {
        val matched = filter(items) { candidate: GlobalCameraEventBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCameraEventBudgets(store: GlobalCameraEventBudgetStore, query: GlobalCameraEventBudgetQuery): MutableList<GlobalCameraEventBudget> {
    return query.apply(store.list())
}
