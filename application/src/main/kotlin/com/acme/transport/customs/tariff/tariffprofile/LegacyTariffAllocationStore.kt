package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy tariff allocations by code. */
interface LegacyTariffAllocationStore {
    fun find(code: String): LegacyTariffAllocation?
    fun save(item: LegacyTariffAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTariffAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTariffAllocationStore(
    var items: MutableMap<String, LegacyTariffAllocation>,
    var order: MutableList<String>,
) : LegacyTariffAllocationStore {
    override fun find(code: String): LegacyTariffAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTariffAllocation) {
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

    override fun list(): MutableList<LegacyTariffAllocation> {
        val out = mutableListOf<LegacyTariffAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTariffAllocationStore(): LegacyTariffAllocationStore {
    return MemoryLegacyTariffAllocationStore(
        mutableMapOf<String, LegacyTariffAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTariffAllocationStore(
    var inner: LegacyTariffAllocationStore,
    var cache: TtlCache<LegacyTariffAllocation>,
    var hits: Int,
    var misses: Int,
) : LegacyTariffAllocationStore {
    override fun find(code: String): LegacyTariffAllocation? {
        val hit = (cache.get(code) as LegacyTariffAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTariffAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTariffAllocation> = inner.list()
}

fun newCachedLegacyTariffAllocationStore(inner: LegacyTariffAllocationStore, ttl: Int, clock: Clock): LegacyTariffAllocationStore {
    return CachedLegacyTariffAllocationStore(
        inner = inner,
        cache = newTtlCache<LegacyTariffAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tariff allocations. Empty fields match all. */
data class LegacyTariffAllocationQuery(
    var unit: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTariffAllocation): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<LegacyTariffAllocation>): MutableList<LegacyTariffAllocation> {
        val matched = filter(items) { candidate: LegacyTariffAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTariffAllocations(store: LegacyTariffAllocationStore, query: LegacyTariffAllocationQuery): MutableList<LegacyTariffAllocation> {
    return query.apply(store.list())
}
