package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan rules by code. */
interface ServicePlanRuleStore {
    fun find(code: String): ServicePlanRule?
    fun save(item: ServicePlanRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanRuleStore(
    var items: MutableMap<String, ServicePlanRule>,
    var order: MutableList<String>,
) : ServicePlanRuleStore {
    override fun find(code: String): ServicePlanRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanRule) {
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

    override fun list(): MutableList<ServicePlanRule> {
        val out = mutableListOf<ServicePlanRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanRuleStore(): ServicePlanRuleStore {
    return MemoryServicePlanRuleStore(mutableMapOf<String, ServicePlanRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanRuleStore(
    var inner: ServicePlanRuleStore,
    var cache: TtlCache<ServicePlanRule>,
    var hits: Int,
    var misses: Int,
) : ServicePlanRuleStore {
    override fun find(code: String): ServicePlanRule? {
        val hit = (cache.get(code) as ServicePlanRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanRule> = inner.list()
}

fun newCachedServicePlanRuleStore(inner: ServicePlanRuleStore, ttl: Int, clock: Clock): ServicePlanRuleStore {
    return CachedServicePlanRuleStore(
        inner = inner,
        cache = newTtlCache<ServicePlanRule>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan rules. Empty fields match all. */
data class ServicePlanRuleQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanRule): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanRule>): MutableList<ServicePlanRule> {
        val matched = filter(items) { candidate: ServicePlanRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanRules(store: ServicePlanRuleStore, query: ServicePlanRuleQuery): MutableList<ServicePlanRule> {
    return query.apply(store.list())
}
