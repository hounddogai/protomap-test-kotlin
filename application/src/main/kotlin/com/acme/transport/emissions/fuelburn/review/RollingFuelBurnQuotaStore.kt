package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel burn quotas by code. */
interface RollingFuelBurnQuotaStore {
    fun find(code: String): RollingFuelBurnQuota?
    fun save(item: RollingFuelBurnQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelBurnQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelBurnQuotaStore(
    var items: MutableMap<String, RollingFuelBurnQuota>,
    var order: MutableList<String>,
) : RollingFuelBurnQuotaStore {
    override fun find(code: String): RollingFuelBurnQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelBurnQuota) {
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

    override fun list(): MutableList<RollingFuelBurnQuota> {
        val out = mutableListOf<RollingFuelBurnQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelBurnQuotaStore(): RollingFuelBurnQuotaStore {
    return MemoryRollingFuelBurnQuotaStore(
        mutableMapOf<String, RollingFuelBurnQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelBurnQuotaStore(
    var inner: RollingFuelBurnQuotaStore,
    var cache: TtlCache<RollingFuelBurnQuota>,
    var hits: Int,
    var misses: Int,
) : RollingFuelBurnQuotaStore {
    override fun find(code: String): RollingFuelBurnQuota? {
        val hit = (cache.get(code) as RollingFuelBurnQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelBurnQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelBurnQuota> = inner.list()
}

fun newCachedRollingFuelBurnQuotaStore(inner: RollingFuelBurnQuotaStore, ttl: Int, clock: Clock): RollingFuelBurnQuotaStore {
    return CachedRollingFuelBurnQuotaStore(
        inner = inner,
        cache = newTtlCache<RollingFuelBurnQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel burn quotas. Empty fields match all. */
data class RollingFuelBurnQuotaQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelBurnQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingFuelBurnQuota>): MutableList<RollingFuelBurnQuota> {
        val matched = filter(items) { candidate: RollingFuelBurnQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelBurnQuotas(store: RollingFuelBurnQuotaStore, query: RollingFuelBurnQuotaQuery): MutableList<RollingFuelBurnQuota> {
    return query.apply(store.list())
}
