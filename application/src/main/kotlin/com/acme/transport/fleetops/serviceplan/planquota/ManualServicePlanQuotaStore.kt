package com.acme.transport.fleetops.serviceplan.planquota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual service plan quotas by code. */
interface ManualServicePlanQuotaStore {
    fun find(code: String): ManualServicePlanQuota?
    fun save(item: ManualServicePlanQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualServicePlanQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualServicePlanQuotaStore(
    var items: MutableMap<String, ManualServicePlanQuota>,
    var order: MutableList<String>,
) : ManualServicePlanQuotaStore {
    override fun find(code: String): ManualServicePlanQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualServicePlanQuota) {
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

    override fun list(): MutableList<ManualServicePlanQuota> {
        val out = mutableListOf<ManualServicePlanQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualServicePlanQuotaStore(): ManualServicePlanQuotaStore {
    return MemoryManualServicePlanQuotaStore(
        mutableMapOf<String, ManualServicePlanQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualServicePlanQuotaStore(
    var inner: ManualServicePlanQuotaStore,
    var cache: TtlCache<ManualServicePlanQuota>,
    var hits: Int,
    var misses: Int,
) : ManualServicePlanQuotaStore {
    override fun find(code: String): ManualServicePlanQuota? {
        val hit = (cache.get(code) as ManualServicePlanQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualServicePlanQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualServicePlanQuota> = inner.list()
}

fun newCachedManualServicePlanQuotaStore(inner: ManualServicePlanQuotaStore, ttl: Int, clock: Clock): ManualServicePlanQuotaStore {
    return CachedManualServicePlanQuotaStore(
        inner = inner,
        cache = newTtlCache<ManualServicePlanQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual service plan quotas. Empty fields match all. */
data class ManualServicePlanQuotaQuery(
    var region: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualServicePlanQuota): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualServicePlanQuota>): MutableList<ManualServicePlanQuota> {
        val matched = filter(items) { candidate: ManualServicePlanQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualServicePlanQuotas(store: ManualServicePlanQuotaStore, query: ManualServicePlanQuotaQuery): MutableList<ManualServicePlanQuota> {
    return query.apply(store.list())
}
