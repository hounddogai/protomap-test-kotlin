package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker plans by code. */
interface BrokerPlanStore {
    fun find(code: String): BrokerPlan?
    fun save(item: BrokerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerPlanStore(
    var items: MutableMap<String, BrokerPlan>,
    var order: MutableList<String>,
) : BrokerPlanStore {
    override fun find(code: String): BrokerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerPlan) {
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

    override fun list(): MutableList<BrokerPlan> {
        val out = mutableListOf<BrokerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerPlanStore(): BrokerPlanStore {
    return MemoryBrokerPlanStore(mutableMapOf<String, BrokerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerPlanStore(
    var inner: BrokerPlanStore,
    var cache: TtlCache<BrokerPlan>,
    var hits: Int,
    var misses: Int,
) : BrokerPlanStore {
    override fun find(code: String): BrokerPlan? {
        val hit = (cache.get(code) as BrokerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerPlan> = inner.list()
}

fun newCachedBrokerPlanStore(inner: BrokerPlanStore, ttl: Int, clock: Clock): BrokerPlanStore {
    return CachedBrokerPlanStore(
        inner = inner,
        cache = newTtlCache<BrokerPlan>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker plans. Empty fields match all. */
data class BrokerPlanQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerPlan): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BrokerPlan>): MutableList<BrokerPlan> {
        val matched = filter(items) { candidate: BrokerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerPlans(store: BrokerPlanStore, query: BrokerPlanQuery): MutableList<BrokerPlan> {
    return query.apply(store.list())
}
