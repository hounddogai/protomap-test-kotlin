package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional zone quotas by code. */
interface RegionalZoneQuotaStore {
    fun find(code: String): RegionalZoneQuota?
    fun save(item: RegionalZoneQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalZoneQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalZoneQuotaStore(
    var items: MutableMap<String, RegionalZoneQuota>,
    var order: MutableList<String>,
) : RegionalZoneQuotaStore {
    override fun find(code: String): RegionalZoneQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalZoneQuota) {
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

    override fun list(): MutableList<RegionalZoneQuota> {
        val out = mutableListOf<RegionalZoneQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalZoneQuotaStore(): RegionalZoneQuotaStore {
    return MemoryRegionalZoneQuotaStore(mutableMapOf<String, RegionalZoneQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalZoneQuotaStore(
    var inner: RegionalZoneQuotaStore,
    var cache: TtlCache<RegionalZoneQuota>,
    var hits: Int,
    var misses: Int,
) : RegionalZoneQuotaStore {
    override fun find(code: String): RegionalZoneQuota? {
        val hit = (cache.get(code) as RegionalZoneQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalZoneQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalZoneQuota> = inner.list()
}

fun newCachedRegionalZoneQuotaStore(inner: RegionalZoneQuotaStore, ttl: Int, clock: Clock): RegionalZoneQuotaStore {
    return CachedRegionalZoneQuotaStore(
        inner = inner,
        cache = newTtlCache<RegionalZoneQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional zone quotas. Empty fields match all. */
data class RegionalZoneQuotaQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalZoneQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalZoneQuota>): MutableList<RegionalZoneQuota> {
        val matched = filter(items) { candidate: RegionalZoneQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalZoneQuotas(store: RegionalZoneQuotaStore, query: RegionalZoneQuotaQuery): MutableList<RegionalZoneQuota> {
    return query.apply(store.list())
}
