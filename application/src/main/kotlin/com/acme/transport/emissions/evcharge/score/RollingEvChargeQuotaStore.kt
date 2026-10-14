package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling ev charge quotas by code. */
interface RollingEvChargeQuotaStore {
    fun find(code: String): RollingEvChargeQuota?
    fun save(item: RollingEvChargeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingEvChargeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingEvChargeQuotaStore(
    var items: MutableMap<String, RollingEvChargeQuota>,
    var order: MutableList<String>,
) : RollingEvChargeQuotaStore {
    override fun find(code: String): RollingEvChargeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingEvChargeQuota) {
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

    override fun list(): MutableList<RollingEvChargeQuota> {
        val out = mutableListOf<RollingEvChargeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingEvChargeQuotaStore(): RollingEvChargeQuotaStore {
    return MemoryRollingEvChargeQuotaStore(
        mutableMapOf<String, RollingEvChargeQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingEvChargeQuotaStore(
    var inner: RollingEvChargeQuotaStore,
    var cache: TtlCache<RollingEvChargeQuota>,
    var hits: Int,
    var misses: Int,
) : RollingEvChargeQuotaStore {
    override fun find(code: String): RollingEvChargeQuota? {
        val hit = (cache.get(code) as RollingEvChargeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingEvChargeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingEvChargeQuota> = inner.list()
}

fun newCachedRollingEvChargeQuotaStore(inner: RollingEvChargeQuotaStore, ttl: Int, clock: Clock): RollingEvChargeQuotaStore {
    return CachedRollingEvChargeQuotaStore(
        inner = inner,
        cache = newTtlCache<RollingEvChargeQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling ev charge quotas. Empty fields match all. */
data class RollingEvChargeQuotaQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingEvChargeQuota): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingEvChargeQuota>): MutableList<RollingEvChargeQuota> {
        val matched = filter(items) { candidate: RollingEvChargeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingEvChargeQuotas(store: RollingEvChargeQuotaStore, query: RollingEvChargeQuotaQuery): MutableList<RollingEvChargeQuota> {
    return query.apply(store.list())
}
