package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps manual hub quotas by code. */
interface ManualHubQuotaStore {
    fun find(code: String): ManualHubQuota?
    fun save(item: ManualHubQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualHubQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualHubQuotaStore(
    var items: MutableMap<String, ManualHubQuota>,
    var order: MutableList<String>,
) : ManualHubQuotaStore {
    override fun find(code: String): ManualHubQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualHubQuota) {
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

    override fun list(): MutableList<ManualHubQuota> {
        val out = mutableListOf<ManualHubQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualHubQuotaStore(): ManualHubQuotaStore {
    return MemoryManualHubQuotaStore(mutableMapOf<String, ManualHubQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualHubQuotaStore(
    var inner: ManualHubQuotaStore,
    var cache: TtlCache<ManualHubQuota>,
    var hits: Int,
    var misses: Int,
) : ManualHubQuotaStore {
    override fun find(code: String): ManualHubQuota? {
        val hit = (cache.get(code) as ManualHubQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualHubQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualHubQuota> = inner.list()
}

fun newCachedManualHubQuotaStore(inner: ManualHubQuotaStore, ttl: Int, clock: Clock): ManualHubQuotaStore {
    return CachedManualHubQuotaStore(
        inner = inner,
        cache = newTtlCache<ManualHubQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual hub quotas. Empty fields match all. */
data class ManualHubQuotaQuery(
    var country: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualHubQuota): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualHubQuota>): MutableList<ManualHubQuota> {
        val matched = filter(items) { candidate: ManualHubQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualHubQuotas(store: ManualHubQuotaStore, query: ManualHubQuotaQuery): MutableList<ManualHubQuota> {
    return query.apply(store.list())
}
