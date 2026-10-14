package com.acme.transport.fleetops.tire.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire quotas by code. */
interface TireQuotaStore {
    fun find(code: String): TireQuota?
    fun save(item: TireQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireQuotaStore(
    var items: MutableMap<String, TireQuota>,
    var order: MutableList<String>,
) : TireQuotaStore {
    override fun find(code: String): TireQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireQuota) {
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

    override fun list(): MutableList<TireQuota> {
        val out = mutableListOf<TireQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireQuotaStore(): TireQuotaStore {
    return MemoryTireQuotaStore(mutableMapOf<String, TireQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireQuotaStore(
    var inner: TireQuotaStore,
    var cache: TtlCache<TireQuota>,
    var hits: Int,
    var misses: Int,
) : TireQuotaStore {
    override fun find(code: String): TireQuota? {
        val hit = (cache.get(code) as TireQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireQuota> = inner.list()
}

fun newCachedTireQuotaStore(inner: TireQuotaStore, ttl: Int, clock: Clock): TireQuotaStore {
    return CachedTireQuotaStore(
        inner = inner,
        cache = newTtlCache<TireQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire quotas. Empty fields match all. */
data class TireQuotaQuery(
    var segment: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireQuota>): MutableList<TireQuota> {
        val matched = filter(items) { candidate: TireQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireQuotas(store: TireQuotaStore, query: TireQuotaQuery): MutableList<TireQuota> {
    return query.apply(store.list())
}
