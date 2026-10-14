package com.acme.transport.customs.tariff.quota

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff quotas by code. */
interface TariffQuotaStore {
    fun find(code: String): TariffQuota?
    fun save(item: TariffQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffQuotaStore(
    var items: MutableMap<String, TariffQuota>,
    var order: MutableList<String>,
) : TariffQuotaStore {
    override fun find(code: String): TariffQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffQuota) {
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

    override fun list(): MutableList<TariffQuota> {
        val out = mutableListOf<TariffQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffQuotaStore(): TariffQuotaStore {
    return MemoryTariffQuotaStore(mutableMapOf<String, TariffQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffQuotaStore(
    var inner: TariffQuotaStore,
    var cache: TtlCache<TariffQuota>,
    var hits: Int,
    var misses: Int,
) : TariffQuotaStore {
    override fun find(code: String): TariffQuota? {
        val hit = (cache.get(code) as TariffQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffQuota> = inner.list()
}

fun newCachedTariffQuotaStore(inner: TariffQuotaStore, ttl: Int, clock: Clock): TariffQuotaStore {
    return CachedTariffQuotaStore(
        inner = inner,
        cache = newTtlCache<TariffQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff quotas. Empty fields match all. */
data class TariffQuotaQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffQuota>): MutableList<TariffQuota> {
        val matched = filter(items) { candidate: TariffQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffQuotas(store: TariffQuotaStore, query: TariffQuotaQuery): MutableList<TariffQuota> {
    return query.apply(store.list())
}
