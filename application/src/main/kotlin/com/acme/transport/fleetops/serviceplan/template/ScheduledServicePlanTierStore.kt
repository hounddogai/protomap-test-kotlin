package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled service plan tiers by code. */
interface ScheduledServicePlanTierStore {
    fun find(code: String): ScheduledServicePlanTier?
    fun save(item: ScheduledServicePlanTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledServicePlanTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledServicePlanTierStore(
    var items: MutableMap<String, ScheduledServicePlanTier>,
    var order: MutableList<String>,
) : ScheduledServicePlanTierStore {
    override fun find(code: String): ScheduledServicePlanTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledServicePlanTier) {
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

    override fun list(): MutableList<ScheduledServicePlanTier> {
        val out = mutableListOf<ScheduledServicePlanTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledServicePlanTierStore(): ScheduledServicePlanTierStore {
    return MemoryScheduledServicePlanTierStore(
        mutableMapOf<String, ScheduledServicePlanTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledServicePlanTierStore(
    var inner: ScheduledServicePlanTierStore,
    var cache: TtlCache<ScheduledServicePlanTier>,
    var hits: Int,
    var misses: Int,
) : ScheduledServicePlanTierStore {
    override fun find(code: String): ScheduledServicePlanTier? {
        val hit = (cache.get(code) as ScheduledServicePlanTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledServicePlanTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledServicePlanTier> = inner.list()
}

fun newCachedScheduledServicePlanTierStore(inner: ScheduledServicePlanTierStore, ttl: Int, clock: Clock): ScheduledServicePlanTierStore {
    return CachedScheduledServicePlanTierStore(
        inner = inner,
        cache = newTtlCache<ScheduledServicePlanTier>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled service plan tiers. Empty fields match all. */
data class ScheduledServicePlanTierQuery(
    var depot: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledServicePlanTier): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledServicePlanTier>): MutableList<ScheduledServicePlanTier> {
        val matched = filter(items) { candidate: ScheduledServicePlanTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledServicePlanTiers(store: ScheduledServicePlanTierStore, query: ScheduledServicePlanTierQuery): MutableList<ScheduledServicePlanTier> {
    return query.apply(store.list())
}
