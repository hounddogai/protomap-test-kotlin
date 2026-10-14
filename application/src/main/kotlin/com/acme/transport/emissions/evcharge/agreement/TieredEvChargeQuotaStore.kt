package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered ev charge quotas by code. */
interface TieredEvChargeQuotaStore {
    fun find(code: String): TieredEvChargeQuota?
    fun save(item: TieredEvChargeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEvChargeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEvChargeQuotaStore(
    var items: MutableMap<String, TieredEvChargeQuota>,
    var order: MutableList<String>,
) : TieredEvChargeQuotaStore {
    override fun find(code: String): TieredEvChargeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEvChargeQuota) {
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

    override fun list(): MutableList<TieredEvChargeQuota> {
        val out = mutableListOf<TieredEvChargeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEvChargeQuotaStore(): TieredEvChargeQuotaStore {
    return MemoryTieredEvChargeQuotaStore(mutableMapOf<String, TieredEvChargeQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEvChargeQuotaStore(
    var inner: TieredEvChargeQuotaStore,
    var cache: TtlCache<TieredEvChargeQuota>,
    var hits: Int,
    var misses: Int,
) : TieredEvChargeQuotaStore {
    override fun find(code: String): TieredEvChargeQuota? {
        val hit = (cache.get(code) as TieredEvChargeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEvChargeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEvChargeQuota> = inner.list()
}

fun newCachedTieredEvChargeQuotaStore(inner: TieredEvChargeQuotaStore, ttl: Int, clock: Clock): TieredEvChargeQuotaStore {
    return CachedTieredEvChargeQuotaStore(
        inner = inner,
        cache = newTtlCache<TieredEvChargeQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered ev charge quotas. Empty fields match all. */
data class TieredEvChargeQuotaQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEvChargeQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredEvChargeQuota>): MutableList<TieredEvChargeQuota> {
        val matched = filter(items) { candidate: TieredEvChargeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEvChargeQuotas(store: TieredEvChargeQuotaStore, query: TieredEvChargeQuotaQuery): MutableList<TieredEvChargeQuota> {
    return query.apply(store.list())
}
