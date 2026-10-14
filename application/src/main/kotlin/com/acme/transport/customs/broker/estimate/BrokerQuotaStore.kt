package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker quotas by code. */
interface BrokerQuotaStore {
    fun find(code: String): BrokerQuota?
    fun save(item: BrokerQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerQuotaStore(
    var items: MutableMap<String, BrokerQuota>,
    var order: MutableList<String>,
) : BrokerQuotaStore {
    override fun find(code: String): BrokerQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerQuota) {
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

    override fun list(): MutableList<BrokerQuota> {
        val out = mutableListOf<BrokerQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerQuotaStore(): BrokerQuotaStore {
    return MemoryBrokerQuotaStore(mutableMapOf<String, BrokerQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerQuotaStore(
    var inner: BrokerQuotaStore,
    var cache: TtlCache<BrokerQuota>,
    var hits: Int,
    var misses: Int,
) : BrokerQuotaStore {
    override fun find(code: String): BrokerQuota? {
        val hit = (cache.get(code) as BrokerQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerQuota> = inner.list()
}

fun newCachedBrokerQuotaStore(inner: BrokerQuotaStore, ttl: Int, clock: Clock): BrokerQuotaStore {
    return CachedBrokerQuotaStore(
        inner = inner,
        cache = newTtlCache<BrokerQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker quotas. Empty fields match all. */
data class BrokerQuotaQuery(
    var region: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerQuota): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerQuota>): MutableList<BrokerQuota> {
        val matched = filter(items) { candidate: BrokerQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerQuotas(store: BrokerQuotaStore, query: BrokerQuotaQuery): MutableList<BrokerQuota> {
    return query.apply(store.list())
}
