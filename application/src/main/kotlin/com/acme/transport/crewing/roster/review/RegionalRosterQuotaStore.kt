package com.acme.transport.crewing.roster.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional roster quotas by code. */
interface RegionalRosterQuotaStore {
    fun find(code: String): RegionalRosterQuota?
    fun save(item: RegionalRosterQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRosterQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRosterQuotaStore(
    var items: MutableMap<String, RegionalRosterQuota>,
    var order: MutableList<String>,
) : RegionalRosterQuotaStore {
    override fun find(code: String): RegionalRosterQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRosterQuota) {
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

    override fun list(): MutableList<RegionalRosterQuota> {
        val out = mutableListOf<RegionalRosterQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRosterQuotaStore(): RegionalRosterQuotaStore {
    return MemoryRegionalRosterQuotaStore(mutableMapOf<String, RegionalRosterQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRosterQuotaStore(
    var inner: RegionalRosterQuotaStore,
    var cache: TtlCache<RegionalRosterQuota>,
    var hits: Int,
    var misses: Int,
) : RegionalRosterQuotaStore {
    override fun find(code: String): RegionalRosterQuota? {
        val hit = (cache.get(code) as RegionalRosterQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRosterQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRosterQuota> = inner.list()
}

fun newCachedRegionalRosterQuotaStore(inner: RegionalRosterQuotaStore, ttl: Int, clock: Clock): RegionalRosterQuotaStore {
    return CachedRegionalRosterQuotaStore(
        inner = inner,
        cache = newTtlCache<RegionalRosterQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional roster quotas. Empty fields match all. */
data class RegionalRosterQuotaQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRosterQuota): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRosterQuota>): MutableList<RegionalRosterQuota> {
        val matched = filter(items) { candidate: RegionalRosterQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRosterQuotas(store: RegionalRosterQuotaStore, query: RegionalRosterQuotaQuery): MutableList<RegionalRosterQuota> {
    return query.apply(store.list())
}
