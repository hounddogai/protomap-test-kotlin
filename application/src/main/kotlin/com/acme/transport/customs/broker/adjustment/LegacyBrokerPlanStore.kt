package com.acme.transport.customs.broker.adjustment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy broker plans by code. */
interface LegacyBrokerPlanStore {
    fun find(code: String): LegacyBrokerPlan?
    fun save(item: LegacyBrokerPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBrokerPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBrokerPlanStore(
    var items: MutableMap<String, LegacyBrokerPlan>,
    var order: MutableList<String>,
) : LegacyBrokerPlanStore {
    override fun find(code: String): LegacyBrokerPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBrokerPlan) {
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

    override fun list(): MutableList<LegacyBrokerPlan> {
        val out = mutableListOf<LegacyBrokerPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBrokerPlanStore(): LegacyBrokerPlanStore {
    return MemoryLegacyBrokerPlanStore(mutableMapOf<String, LegacyBrokerPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBrokerPlanStore(
    var inner: LegacyBrokerPlanStore,
    var cache: TtlCache<LegacyBrokerPlan>,
    var hits: Int,
    var misses: Int,
) : LegacyBrokerPlanStore {
    override fun find(code: String): LegacyBrokerPlan? {
        val hit = (cache.get(code) as LegacyBrokerPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBrokerPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBrokerPlan> = inner.list()
}

fun newCachedLegacyBrokerPlanStore(inner: LegacyBrokerPlanStore, ttl: Int, clock: Clock): LegacyBrokerPlanStore {
    return CachedLegacyBrokerPlanStore(
        inner = inner,
        cache = newTtlCache<LegacyBrokerPlan>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy broker plans. Empty fields match all. */
data class LegacyBrokerPlanQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBrokerPlan): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBrokerPlan>): MutableList<LegacyBrokerPlan> {
        val matched = filter(items) { candidate: LegacyBrokerPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBrokerPlans(store: LegacyBrokerPlanStore, query: LegacyBrokerPlanQuery): MutableList<LegacyBrokerPlan> {
    return query.apply(store.list())
}
