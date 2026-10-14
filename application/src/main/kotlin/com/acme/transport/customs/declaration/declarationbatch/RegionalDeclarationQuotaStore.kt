package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional declaration quotas by code. */
interface RegionalDeclarationQuotaStore {
    fun find(code: String): RegionalDeclarationQuota?
    fun save(item: RegionalDeclarationQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDeclarationQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDeclarationQuotaStore(
    var items: MutableMap<String, RegionalDeclarationQuota>,
    var order: MutableList<String>,
) : RegionalDeclarationQuotaStore {
    override fun find(code: String): RegionalDeclarationQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDeclarationQuota) {
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

    override fun list(): MutableList<RegionalDeclarationQuota> {
        val out = mutableListOf<RegionalDeclarationQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDeclarationQuotaStore(): RegionalDeclarationQuotaStore {
    return MemoryRegionalDeclarationQuotaStore(
        mutableMapOf<String, RegionalDeclarationQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDeclarationQuotaStore(
    var inner: RegionalDeclarationQuotaStore,
    var cache: TtlCache<RegionalDeclarationQuota>,
    var hits: Int,
    var misses: Int,
) : RegionalDeclarationQuotaStore {
    override fun find(code: String): RegionalDeclarationQuota? {
        val hit = (cache.get(code) as RegionalDeclarationQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDeclarationQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDeclarationQuota> = inner.list()
}

fun newCachedRegionalDeclarationQuotaStore(inner: RegionalDeclarationQuotaStore, ttl: Int, clock: Clock): RegionalDeclarationQuotaStore {
    return CachedRegionalDeclarationQuotaStore(
        inner = inner,
        cache = newTtlCache<RegionalDeclarationQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional declaration quotas. Empty fields match all. */
data class RegionalDeclarationQuotaQuery(
    var segment: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDeclarationQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDeclarationQuota>): MutableList<RegionalDeclarationQuota> {
        val matched = filter(items) { candidate: RegionalDeclarationQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDeclarationQuotas(store: RegionalDeclarationQuotaStore, query: RegionalDeclarationQuotaQuery): MutableList<RegionalDeclarationQuota> {
    return query.apply(store.list())
}
