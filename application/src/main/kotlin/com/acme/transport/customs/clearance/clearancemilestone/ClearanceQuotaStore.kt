package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance quotas by code. */
interface ClearanceQuotaStore {
    fun find(code: String): ClearanceQuota?
    fun save(item: ClearanceQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceQuotaStore(
    var items: MutableMap<String, ClearanceQuota>,
    var order: MutableList<String>,
) : ClearanceQuotaStore {
    override fun find(code: String): ClearanceQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceQuota) {
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

    override fun list(): MutableList<ClearanceQuota> {
        val out = mutableListOf<ClearanceQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceQuotaStore(): ClearanceQuotaStore {
    return MemoryClearanceQuotaStore(mutableMapOf<String, ClearanceQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceQuotaStore(
    var inner: ClearanceQuotaStore,
    var cache: TtlCache<ClearanceQuota>,
    var hits: Int,
    var misses: Int,
) : ClearanceQuotaStore {
    override fun find(code: String): ClearanceQuota? {
        val hit = (cache.get(code) as ClearanceQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceQuota> = inner.list()
}

fun newCachedClearanceQuotaStore(inner: ClearanceQuotaStore, ttl: Int, clock: Clock): ClearanceQuotaStore {
    return CachedClearanceQuotaStore(
        inner = inner,
        cache = newTtlCache<ClearanceQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance quotas. Empty fields match all. */
data class ClearanceQuotaQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceQuota>): MutableList<ClearanceQuota> {
        val matched = filter(items) { candidate: ClearanceQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceQuotas(store: ClearanceQuotaStore, query: ClearanceQuotaQuery): MutableList<ClearanceQuota> {
    return query.apply(store.list())
}
