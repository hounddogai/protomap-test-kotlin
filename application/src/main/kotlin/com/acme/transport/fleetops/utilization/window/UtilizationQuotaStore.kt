package com.acme.transport.fleetops.utilization.window

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization quotas by code. */
interface UtilizationQuotaStore {
    fun find(code: String): UtilizationQuota?
    fun save(item: UtilizationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationQuotaStore(
    var items: MutableMap<String, UtilizationQuota>,
    var order: MutableList<String>,
) : UtilizationQuotaStore {
    override fun find(code: String): UtilizationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationQuota) {
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

    override fun list(): MutableList<UtilizationQuota> {
        val out = mutableListOf<UtilizationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationQuotaStore(): UtilizationQuotaStore {
    return MemoryUtilizationQuotaStore(mutableMapOf<String, UtilizationQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationQuotaStore(
    var inner: UtilizationQuotaStore,
    var cache: TtlCache<UtilizationQuota>,
    var hits: Int,
    var misses: Int,
) : UtilizationQuotaStore {
    override fun find(code: String): UtilizationQuota? {
        val hit = (cache.get(code) as UtilizationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationQuota> = inner.list()
}

fun newCachedUtilizationQuotaStore(inner: UtilizationQuotaStore, ttl: Int, clock: Clock): UtilizationQuotaStore {
    return CachedUtilizationQuotaStore(
        inner = inner,
        cache = newTtlCache<UtilizationQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization quotas. Empty fields match all. */
data class UtilizationQuotaQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationQuota>): MutableList<UtilizationQuota> {
        val matched = filter(items) { candidate: UtilizationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationQuotas(store: UtilizationQuotaStore, query: UtilizationQuotaQuery): MutableList<UtilizationQuota> {
    return query.apply(store.list())
}
