package com.acme.transport.customs.broker.snapshot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling broker quotas by code. */
interface RollingBrokerQuotaStore {
    fun find(code: String): RollingBrokerQuota?
    fun save(item: RollingBrokerQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBrokerQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBrokerQuotaStore(
    var items: MutableMap<String, RollingBrokerQuota>,
    var order: MutableList<String>,
) : RollingBrokerQuotaStore {
    override fun find(code: String): RollingBrokerQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBrokerQuota) {
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

    override fun list(): MutableList<RollingBrokerQuota> {
        val out = mutableListOf<RollingBrokerQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBrokerQuotaStore(): RollingBrokerQuotaStore {
    return MemoryRollingBrokerQuotaStore(mutableMapOf<String, RollingBrokerQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBrokerQuotaStore(
    var inner: RollingBrokerQuotaStore,
    var cache: TtlCache<RollingBrokerQuota>,
    var hits: Int,
    var misses: Int,
) : RollingBrokerQuotaStore {
    override fun find(code: String): RollingBrokerQuota? {
        val hit = (cache.get(code) as RollingBrokerQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBrokerQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBrokerQuota> = inner.list()
}

fun newCachedRollingBrokerQuotaStore(inner: RollingBrokerQuotaStore, ttl: Int, clock: Clock): RollingBrokerQuotaStore {
    return CachedRollingBrokerQuotaStore(
        inner = inner,
        cache = newTtlCache<RollingBrokerQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling broker quotas. Empty fields match all. */
data class RollingBrokerQuotaQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBrokerQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingBrokerQuota>): MutableList<RollingBrokerQuota> {
        val matched = filter(items) { candidate: RollingBrokerQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBrokerQuotas(store: RollingBrokerQuotaStore, query: RollingBrokerQuotaQuery): MutableList<RollingBrokerQuota> {
    return query.apply(store.list())
}
