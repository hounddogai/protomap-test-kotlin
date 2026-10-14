package com.acme.transport.customs.tariff.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy tariff quotas by code. */
interface LegacyTariffQuotaStore {
    fun find(code: String): LegacyTariffQuota?
    fun save(item: LegacyTariffQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTariffQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTariffQuotaStore(
    var items: MutableMap<String, LegacyTariffQuota>,
    var order: MutableList<String>,
) : LegacyTariffQuotaStore {
    override fun find(code: String): LegacyTariffQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTariffQuota) {
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

    override fun list(): MutableList<LegacyTariffQuota> {
        val out = mutableListOf<LegacyTariffQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTariffQuotaStore(): LegacyTariffQuotaStore {
    return MemoryLegacyTariffQuotaStore(mutableMapOf<String, LegacyTariffQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTariffQuotaStore(
    var inner: LegacyTariffQuotaStore,
    var cache: TtlCache<LegacyTariffQuota>,
    var hits: Int,
    var misses: Int,
) : LegacyTariffQuotaStore {
    override fun find(code: String): LegacyTariffQuota? {
        val hit = (cache.get(code) as LegacyTariffQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTariffQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTariffQuota> = inner.list()
}

fun newCachedLegacyTariffQuotaStore(inner: LegacyTariffQuotaStore, ttl: Int, clock: Clock): LegacyTariffQuotaStore {
    return CachedLegacyTariffQuotaStore(
        inner = inner,
        cache = newTtlCache<LegacyTariffQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tariff quotas. Empty fields match all. */
data class LegacyTariffQuotaQuery(
    var channel: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTariffQuota): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTariffQuota>): MutableList<LegacyTariffQuota> {
        val matched = filter(items) { candidate: LegacyTariffQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTariffQuotas(store: LegacyTariffQuotaStore, query: LegacyTariffQuotaQuery): MutableList<LegacyTariffQuota> {
    return query.apply(store.list())
}
