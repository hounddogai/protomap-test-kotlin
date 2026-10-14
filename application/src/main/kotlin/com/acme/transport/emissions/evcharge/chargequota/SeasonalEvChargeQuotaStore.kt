package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal ev charge quotas by code. */
interface SeasonalEvChargeQuotaStore {
    fun find(code: String): SeasonalEvChargeQuota?
    fun save(item: SeasonalEvChargeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalEvChargeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalEvChargeQuotaStore(
    var items: MutableMap<String, SeasonalEvChargeQuota>,
    var order: MutableList<String>,
) : SeasonalEvChargeQuotaStore {
    override fun find(code: String): SeasonalEvChargeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalEvChargeQuota) {
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

    override fun list(): MutableList<SeasonalEvChargeQuota> {
        val out = mutableListOf<SeasonalEvChargeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalEvChargeQuotaStore(): SeasonalEvChargeQuotaStore {
    return MemorySeasonalEvChargeQuotaStore(
        mutableMapOf<String, SeasonalEvChargeQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalEvChargeQuotaStore(
    var inner: SeasonalEvChargeQuotaStore,
    var cache: TtlCache<SeasonalEvChargeQuota>,
    var hits: Int,
    var misses: Int,
) : SeasonalEvChargeQuotaStore {
    override fun find(code: String): SeasonalEvChargeQuota? {
        val hit = (cache.get(code) as SeasonalEvChargeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalEvChargeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalEvChargeQuota> = inner.list()
}

fun newCachedSeasonalEvChargeQuotaStore(inner: SeasonalEvChargeQuotaStore, ttl: Int, clock: Clock): SeasonalEvChargeQuotaStore {
    return CachedSeasonalEvChargeQuotaStore(
        inner = inner,
        cache = newTtlCache<SeasonalEvChargeQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal ev charge quotas. Empty fields match all. */
data class SeasonalEvChargeQuotaQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalEvChargeQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalEvChargeQuota>): MutableList<SeasonalEvChargeQuota> {
        val matched = filter(items) { candidate: SeasonalEvChargeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalEvChargeQuotas(store: SeasonalEvChargeQuotaStore, query: SeasonalEvChargeQuotaQuery): MutableList<SeasonalEvChargeQuota> {
    return query.apply(store.list())
}
