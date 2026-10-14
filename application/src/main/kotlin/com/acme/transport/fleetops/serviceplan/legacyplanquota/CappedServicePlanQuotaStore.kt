package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped service plan quotas by code. */
interface CappedServicePlanQuotaStore {
    fun find(code: String): CappedServicePlanQuota?
    fun save(item: CappedServicePlanQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServicePlanQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServicePlanQuotaStore(
    var items: MutableMap<String, CappedServicePlanQuota>,
    var order: MutableList<String>,
) : CappedServicePlanQuotaStore {
    override fun find(code: String): CappedServicePlanQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServicePlanQuota) {
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

    override fun list(): MutableList<CappedServicePlanQuota> {
        val out = mutableListOf<CappedServicePlanQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServicePlanQuotaStore(): CappedServicePlanQuotaStore {
    return MemoryCappedServicePlanQuotaStore(
        mutableMapOf<String, CappedServicePlanQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServicePlanQuotaStore(
    var inner: CappedServicePlanQuotaStore,
    var cache: TtlCache<CappedServicePlanQuota>,
    var hits: Int,
    var misses: Int,
) : CappedServicePlanQuotaStore {
    override fun find(code: String): CappedServicePlanQuota? {
        val hit = (cache.get(code) as CappedServicePlanQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServicePlanQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServicePlanQuota> = inner.list()
}

fun newCachedCappedServicePlanQuotaStore(inner: CappedServicePlanQuotaStore, ttl: Int, clock: Clock): CappedServicePlanQuotaStore {
    return CachedCappedServicePlanQuotaStore(
        inner = inner,
        cache = newTtlCache<CappedServicePlanQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service plan quotas. Empty fields match all. */
data class CappedServicePlanQuotaQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServicePlanQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedServicePlanQuota>): MutableList<CappedServicePlanQuota> {
        val matched = filter(items) { candidate: CappedServicePlanQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServicePlanQuotas(store: CappedServicePlanQuotaStore, query: CappedServicePlanQuotaQuery): MutableList<CappedServicePlanQuota> {
    return query.apply(store.list())
}
