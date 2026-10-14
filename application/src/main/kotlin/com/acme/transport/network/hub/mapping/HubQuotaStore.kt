package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub quotas by code. */
interface HubQuotaStore {
    fun find(code: String): HubQuota?
    fun save(item: HubQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubQuotaStore(
    var items: MutableMap<String, HubQuota>,
    var order: MutableList<String>,
) : HubQuotaStore {
    override fun find(code: String): HubQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubQuota) {
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

    override fun list(): MutableList<HubQuota> {
        val out = mutableListOf<HubQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubQuotaStore(): HubQuotaStore {
    return MemoryHubQuotaStore(mutableMapOf<String, HubQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubQuotaStore(
    var inner: HubQuotaStore,
    var cache: TtlCache<HubQuota>,
    var hits: Int,
    var misses: Int,
) : HubQuotaStore {
    override fun find(code: String): HubQuota? {
        val hit = (cache.get(code) as HubQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubQuota> = inner.list()
}

fun newCachedHubQuotaStore(inner: HubQuotaStore, ttl: Int, clock: Clock): HubQuotaStore {
    return CachedHubQuotaStore(
        inner = inner,
        cache = newTtlCache<HubQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub quotas. Empty fields match all. */
data class HubQuotaQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubQuota>): MutableList<HubQuota> {
        val matched = filter(items) { candidate: HubQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubQuotas(store: HubQuotaStore, query: HubQuotaQuery): MutableList<HubQuota> = query.apply(store.list())
