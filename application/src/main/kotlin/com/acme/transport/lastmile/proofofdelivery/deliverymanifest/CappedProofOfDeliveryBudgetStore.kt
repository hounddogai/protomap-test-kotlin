package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped proof of delivery budgets by code. */
interface CappedProofOfDeliveryBudgetStore {
    fun find(code: String): CappedProofOfDeliveryBudget?
    fun save(item: CappedProofOfDeliveryBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedProofOfDeliveryBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedProofOfDeliveryBudgetStore(
    var items: MutableMap<String, CappedProofOfDeliveryBudget>,
    var order: MutableList<String>,
) : CappedProofOfDeliveryBudgetStore {
    override fun find(code: String): CappedProofOfDeliveryBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedProofOfDeliveryBudget) {
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

    override fun list(): MutableList<CappedProofOfDeliveryBudget> {
        val out = mutableListOf<CappedProofOfDeliveryBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedProofOfDeliveryBudgetStore(): CappedProofOfDeliveryBudgetStore {
    return MemoryCappedProofOfDeliveryBudgetStore(
        mutableMapOf<String, CappedProofOfDeliveryBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedProofOfDeliveryBudgetStore(
    var inner: CappedProofOfDeliveryBudgetStore,
    var cache: TtlCache<CappedProofOfDeliveryBudget>,
    var hits: Int,
    var misses: Int,
) : CappedProofOfDeliveryBudgetStore {
    override fun find(code: String): CappedProofOfDeliveryBudget? {
        val hit = (cache.get(code) as CappedProofOfDeliveryBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedProofOfDeliveryBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedProofOfDeliveryBudget> = inner.list()
}

fun newCachedCappedProofOfDeliveryBudgetStore(inner: CappedProofOfDeliveryBudgetStore, ttl: Int, clock: Clock): CappedProofOfDeliveryBudgetStore {
    return CachedCappedProofOfDeliveryBudgetStore(
        inner = inner,
        cache = newTtlCache<CappedProofOfDeliveryBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped proof of delivery budgets. Empty fields match all. */
data class CappedProofOfDeliveryBudgetQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedProofOfDeliveryBudget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedProofOfDeliveryBudget>): MutableList<CappedProofOfDeliveryBudget> {
        val matched = filter(items) { candidate: CappedProofOfDeliveryBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedProofOfDeliveryBudgets(store: CappedProofOfDeliveryBudgetStore, query: CappedProofOfDeliveryBudgetQuery): MutableList<CappedProofOfDeliveryBudget> {
    return query.apply(store.list())
}
