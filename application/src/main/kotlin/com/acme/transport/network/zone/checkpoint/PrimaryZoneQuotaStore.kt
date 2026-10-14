package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary zone quotas by code. */
interface PrimaryZoneQuotaStore {
    fun find(code: String): PrimaryZoneQuota?
    fun save(item: PrimaryZoneQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryZoneQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryZoneQuotaStore(
    var items: MutableMap<String, PrimaryZoneQuota>,
    var order: MutableList<String>,
) : PrimaryZoneQuotaStore {
    override fun find(code: String): PrimaryZoneQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryZoneQuota) {
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

    override fun list(): MutableList<PrimaryZoneQuota> {
        val out = mutableListOf<PrimaryZoneQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryZoneQuotaStore(): PrimaryZoneQuotaStore {
    return MemoryPrimaryZoneQuotaStore(mutableMapOf<String, PrimaryZoneQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryZoneQuotaStore(
    var inner: PrimaryZoneQuotaStore,
    var cache: TtlCache<PrimaryZoneQuota>,
    var hits: Int,
    var misses: Int,
) : PrimaryZoneQuotaStore {
    override fun find(code: String): PrimaryZoneQuota? {
        val hit = (cache.get(code) as PrimaryZoneQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryZoneQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryZoneQuota> = inner.list()
}

fun newCachedPrimaryZoneQuotaStore(inner: PrimaryZoneQuotaStore, ttl: Int, clock: Clock): PrimaryZoneQuotaStore {
    return CachedPrimaryZoneQuotaStore(
        inner = inner,
        cache = newTtlCache<PrimaryZoneQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary zone quotas. Empty fields match all. */
data class PrimaryZoneQuotaQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryZoneQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryZoneQuota>): MutableList<PrimaryZoneQuota> {
        val matched = filter(items) { candidate: PrimaryZoneQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryZoneQuotas(store: PrimaryZoneQuotaStore, query: PrimaryZoneQuotaQuery): MutableList<PrimaryZoneQuota> {
    return query.apply(store.list())
}
