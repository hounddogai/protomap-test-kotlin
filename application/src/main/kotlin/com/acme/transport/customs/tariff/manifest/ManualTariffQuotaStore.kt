package com.acme.transport.customs.tariff.manifest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual tariff quotas by code. */
interface ManualTariffQuotaStore {
    fun find(code: String): ManualTariffQuota?
    fun save(item: ManualTariffQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualTariffQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualTariffQuotaStore(
    var items: MutableMap<String, ManualTariffQuota>,
    var order: MutableList<String>,
) : ManualTariffQuotaStore {
    override fun find(code: String): ManualTariffQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualTariffQuota) {
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

    override fun list(): MutableList<ManualTariffQuota> {
        val out = mutableListOf<ManualTariffQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualTariffQuotaStore(): ManualTariffQuotaStore {
    return MemoryManualTariffQuotaStore(mutableMapOf<String, ManualTariffQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualTariffQuotaStore(
    var inner: ManualTariffQuotaStore,
    var cache: TtlCache<ManualTariffQuota>,
    var hits: Int,
    var misses: Int,
) : ManualTariffQuotaStore {
    override fun find(code: String): ManualTariffQuota? {
        val hit = (cache.get(code) as ManualTariffQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualTariffQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualTariffQuota> = inner.list()
}

fun newCachedManualTariffQuotaStore(inner: ManualTariffQuotaStore, ttl: Int, clock: Clock): ManualTariffQuotaStore {
    return CachedManualTariffQuotaStore(
        inner = inner,
        cache = newTtlCache<ManualTariffQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual tariff quotas. Empty fields match all. */
data class ManualTariffQuotaQuery(
    var locale: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualTariffQuota): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<ManualTariffQuota>): MutableList<ManualTariffQuota> {
        val matched = filter(items) { candidate: ManualTariffQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualTariffQuotas(store: ManualTariffQuotaStore, query: ManualTariffQuotaQuery): MutableList<ManualTariffQuota> {
    return query.apply(store.list())
}
