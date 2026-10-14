package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered tender budgets by code. */
interface TieredTenderBudgetStore {
    fun find(code: String): TieredTenderBudget?
    fun save(item: TieredTenderBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTenderBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTenderBudgetStore(
    var items: MutableMap<String, TieredTenderBudget>,
    var order: MutableList<String>,
) : TieredTenderBudgetStore {
    override fun find(code: String): TieredTenderBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTenderBudget) {
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

    override fun list(): MutableList<TieredTenderBudget> {
        val out = mutableListOf<TieredTenderBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTenderBudgetStore(): TieredTenderBudgetStore {
    return MemoryTieredTenderBudgetStore(mutableMapOf<String, TieredTenderBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTenderBudgetStore(
    var inner: TieredTenderBudgetStore,
    var cache: TtlCache<TieredTenderBudget>,
    var hits: Int,
    var misses: Int,
) : TieredTenderBudgetStore {
    override fun find(code: String): TieredTenderBudget? {
        val hit = (cache.get(code) as TieredTenderBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTenderBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTenderBudget> = inner.list()
}

fun newCachedTieredTenderBudgetStore(inner: TieredTenderBudgetStore, ttl: Int, clock: Clock): TieredTenderBudgetStore {
    return CachedTieredTenderBudgetStore(
        inner = inner,
        cache = newTtlCache<TieredTenderBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tender budgets. Empty fields match all. */
data class TieredTenderBudgetQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTenderBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTenderBudget>): MutableList<TieredTenderBudget> {
        val matched = filter(items) { candidate: TieredTenderBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTenderBudgets(store: TieredTenderBudgetStore, query: TieredTenderBudgetQuery): MutableList<TieredTenderBudget> {
    return query.apply(store.list())
}
