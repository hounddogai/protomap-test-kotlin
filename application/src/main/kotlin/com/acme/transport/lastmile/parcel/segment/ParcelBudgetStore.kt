package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel budgets by code. */
interface ParcelBudgetStore {
    fun find(code: String): ParcelBudget?
    fun save(item: ParcelBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelBudgetStore(
    var items: MutableMap<String, ParcelBudget>,
    var order: MutableList<String>,
) : ParcelBudgetStore {
    override fun find(code: String): ParcelBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelBudget) {
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

    override fun list(): MutableList<ParcelBudget> {
        val out = mutableListOf<ParcelBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelBudgetStore(): ParcelBudgetStore {
    return MemoryParcelBudgetStore(mutableMapOf<String, ParcelBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelBudgetStore(
    var inner: ParcelBudgetStore,
    var cache: TtlCache<ParcelBudget>,
    var hits: Int,
    var misses: Int,
) : ParcelBudgetStore {
    override fun find(code: String): ParcelBudget? {
        val hit = (cache.get(code) as ParcelBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelBudget> = inner.list()
}

fun newCachedParcelBudgetStore(inner: ParcelBudgetStore, ttl: Int, clock: Clock): ParcelBudgetStore {
    return CachedParcelBudgetStore(
        inner = inner,
        cache = newTtlCache<ParcelBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel budgets. Empty fields match all. */
data class ParcelBudgetQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ParcelBudget>): MutableList<ParcelBudget> {
        val matched = filter(items) { candidate: ParcelBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelBudgets(store: ParcelBudgetStore, query: ParcelBudgetQuery): MutableList<ParcelBudget> {
    return query.apply(store.list())
}
