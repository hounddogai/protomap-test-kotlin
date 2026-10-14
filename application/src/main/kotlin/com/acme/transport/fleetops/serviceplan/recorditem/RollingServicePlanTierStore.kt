package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling service plan tiers by code. */
interface RollingServicePlanTierStore {
    fun find(code: String): RollingServicePlanTier?
    fun save(item: RollingServicePlanTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingServicePlanTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingServicePlanTierStore(
    var items: MutableMap<String, RollingServicePlanTier>,
    var order: MutableList<String>,
) : RollingServicePlanTierStore {
    override fun find(code: String): RollingServicePlanTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingServicePlanTier) {
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

    override fun list(): MutableList<RollingServicePlanTier> {
        val out = mutableListOf<RollingServicePlanTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingServicePlanTierStore(): RollingServicePlanTierStore {
    return MemoryRollingServicePlanTierStore(
        mutableMapOf<String, RollingServicePlanTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingServicePlanTierStore(
    var inner: RollingServicePlanTierStore,
    var cache: TtlCache<RollingServicePlanTier>,
    var hits: Int,
    var misses: Int,
) : RollingServicePlanTierStore {
    override fun find(code: String): RollingServicePlanTier? {
        val hit = (cache.get(code) as RollingServicePlanTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingServicePlanTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingServicePlanTier> = inner.list()
}

fun newCachedRollingServicePlanTierStore(inner: RollingServicePlanTierStore, ttl: Int, clock: Clock): RollingServicePlanTierStore {
    return CachedRollingServicePlanTierStore(
        inner = inner,
        cache = newTtlCache<RollingServicePlanTier>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling service plan tiers. Empty fields match all. */
data class RollingServicePlanTierQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingServicePlanTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingServicePlanTier>): MutableList<RollingServicePlanTier> {
        val matched = filter(items) { candidate: RollingServicePlanTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingServicePlanTiers(store: RollingServicePlanTierStore, query: RollingServicePlanTierQuery): MutableList<RollingServicePlanTier> {
    return query.apply(store.list())
}
