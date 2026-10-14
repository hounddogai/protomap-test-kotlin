package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn quotas by code. */
interface FuelBurnQuotaStore {
    fun find(code: String): FuelBurnQuota?
    fun save(item: FuelBurnQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnQuotaStore(
    var items: MutableMap<String, FuelBurnQuota>,
    var order: MutableList<String>,
) : FuelBurnQuotaStore {
    override fun find(code: String): FuelBurnQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnQuota) {
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

    override fun list(): MutableList<FuelBurnQuota> {
        val out = mutableListOf<FuelBurnQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnQuotaStore(): FuelBurnQuotaStore {
    return MemoryFuelBurnQuotaStore(mutableMapOf<String, FuelBurnQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnQuotaStore(
    var inner: FuelBurnQuotaStore,
    var cache: TtlCache<FuelBurnQuota>,
    var hits: Int,
    var misses: Int,
) : FuelBurnQuotaStore {
    override fun find(code: String): FuelBurnQuota? {
        val hit = (cache.get(code) as FuelBurnQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnQuota> = inner.list()
}

fun newCachedFuelBurnQuotaStore(inner: FuelBurnQuotaStore, ttl: Int, clock: Clock): FuelBurnQuotaStore {
    return CachedFuelBurnQuotaStore(
        inner = inner,
        cache = newTtlCache<FuelBurnQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn quotas. Empty fields match all. */
data class FuelBurnQuotaQuery(
    var title: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnQuota>): MutableList<FuelBurnQuota> {
        val matched = filter(items) { candidate: FuelBurnQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnQuotas(store: FuelBurnQuotaStore, query: FuelBurnQuotaQuery): MutableList<FuelBurnQuota> {
    return query.apply(store.list())
}
