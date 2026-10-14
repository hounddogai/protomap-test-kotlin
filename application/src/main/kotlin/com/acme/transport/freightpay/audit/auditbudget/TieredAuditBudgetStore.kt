package com.acme.transport.freightpay.audit.auditbudget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered audit budgets by code. */
interface TieredAuditBudgetStore {
    fun find(code: String): TieredAuditBudget?
    fun save(item: TieredAuditBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAuditBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAuditBudgetStore(
    var items: MutableMap<String, TieredAuditBudget>,
    var order: MutableList<String>,
) : TieredAuditBudgetStore {
    override fun find(code: String): TieredAuditBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAuditBudget) {
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

    override fun list(): MutableList<TieredAuditBudget> {
        val out = mutableListOf<TieredAuditBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAuditBudgetStore(): TieredAuditBudgetStore {
    return MemoryTieredAuditBudgetStore(mutableMapOf<String, TieredAuditBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAuditBudgetStore(
    var inner: TieredAuditBudgetStore,
    var cache: TtlCache<TieredAuditBudget>,
    var hits: Int,
    var misses: Int,
) : TieredAuditBudgetStore {
    override fun find(code: String): TieredAuditBudget? {
        val hit = (cache.get(code) as TieredAuditBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAuditBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAuditBudget> = inner.list()
}

fun newCachedTieredAuditBudgetStore(inner: TieredAuditBudgetStore, ttl: Int, clock: Clock): TieredAuditBudgetStore {
    return CachedTieredAuditBudgetStore(
        inner = inner,
        cache = newTtlCache<TieredAuditBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered audit budgets. Empty fields match all. */
data class TieredAuditBudgetQuery(
    var country: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAuditBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAuditBudget>): MutableList<TieredAuditBudget> {
        val matched = filter(items) { candidate: TieredAuditBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAuditBudgets(store: TieredAuditBudgetStore, query: TieredAuditBudgetQuery): MutableList<TieredAuditBudget> {
    return query.apply(store.list())
}
