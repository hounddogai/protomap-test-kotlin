package com.acme.transport.safety.nearmiss.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending near miss budgets by code. */
interface PendingNearMissBudgetStore {
    fun find(code: String): PendingNearMissBudget?
    fun save(item: PendingNearMissBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingNearMissBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingNearMissBudgetStore(
    var items: MutableMap<String, PendingNearMissBudget>,
    var order: MutableList<String>,
) : PendingNearMissBudgetStore {
    override fun find(code: String): PendingNearMissBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingNearMissBudget) {
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

    override fun list(): MutableList<PendingNearMissBudget> {
        val out = mutableListOf<PendingNearMissBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingNearMissBudgetStore(): PendingNearMissBudgetStore {
    return MemoryPendingNearMissBudgetStore(
        mutableMapOf<String, PendingNearMissBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingNearMissBudgetStore(
    var inner: PendingNearMissBudgetStore,
    var cache: TtlCache<PendingNearMissBudget>,
    var hits: Int,
    var misses: Int,
) : PendingNearMissBudgetStore {
    override fun find(code: String): PendingNearMissBudget? {
        val hit = (cache.get(code) as PendingNearMissBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingNearMissBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingNearMissBudget> = inner.list()
}

fun newCachedPendingNearMissBudgetStore(inner: PendingNearMissBudgetStore, ttl: Int, clock: Clock): PendingNearMissBudgetStore {
    return CachedPendingNearMissBudgetStore(
        inner = inner,
        cache = newTtlCache<PendingNearMissBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending near miss budgets. Empty fields match all. */
data class PendingNearMissBudgetQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingNearMissBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingNearMissBudget>): MutableList<PendingNearMissBudget> {
        val matched = filter(items) { candidate: PendingNearMissBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingNearMissBudgets(store: PendingNearMissBudgetStore, query: PendingNearMissBudgetQuery): MutableList<PendingNearMissBudget> {
    return query.apply(store.list())
}
