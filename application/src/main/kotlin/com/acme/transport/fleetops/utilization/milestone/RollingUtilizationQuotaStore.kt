package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling utilization quotas by code. */
interface RollingUtilizationQuotaStore {
    fun find(code: String): RollingUtilizationQuota?
    fun save(item: RollingUtilizationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingUtilizationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingUtilizationQuotaStore(
    var items: MutableMap<String, RollingUtilizationQuota>,
    var order: MutableList<String>,
) : RollingUtilizationQuotaStore {
    override fun find(code: String): RollingUtilizationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingUtilizationQuota) {
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

    override fun list(): MutableList<RollingUtilizationQuota> {
        val out = mutableListOf<RollingUtilizationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingUtilizationQuotaStore(): RollingUtilizationQuotaStore {
    return MemoryRollingUtilizationQuotaStore(
        mutableMapOf<String, RollingUtilizationQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingUtilizationQuotaStore(
    var inner: RollingUtilizationQuotaStore,
    var cache: TtlCache<RollingUtilizationQuota>,
    var hits: Int,
    var misses: Int,
) : RollingUtilizationQuotaStore {
    override fun find(code: String): RollingUtilizationQuota? {
        val hit = (cache.get(code) as RollingUtilizationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingUtilizationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingUtilizationQuota> = inner.list()
}

fun newCachedRollingUtilizationQuotaStore(inner: RollingUtilizationQuotaStore, ttl: Int, clock: Clock): RollingUtilizationQuotaStore {
    return CachedRollingUtilizationQuotaStore(
        inner = inner,
        cache = newTtlCache<RollingUtilizationQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling utilization quotas. Empty fields match all. */
data class RollingUtilizationQuotaQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingUtilizationQuota): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingUtilizationQuota>): MutableList<RollingUtilizationQuota> {
        val matched = filter(items) { candidate: RollingUtilizationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingUtilizationQuotas(store: RollingUtilizationQuotaStore, query: RollingUtilizationQuotaQuery): MutableList<RollingUtilizationQuota> {
    return query.apply(store.list())
}
