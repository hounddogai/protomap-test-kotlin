package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report allocations by code. */
interface CarbonReportAllocationStore {
    fun find(code: String): CarbonReportAllocation?
    fun save(item: CarbonReportAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportAllocationStore(
    var items: MutableMap<String, CarbonReportAllocation>,
    var order: MutableList<String>,
) : CarbonReportAllocationStore {
    override fun find(code: String): CarbonReportAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportAllocation) {
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

    override fun list(): MutableList<CarbonReportAllocation> {
        val out = mutableListOf<CarbonReportAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportAllocationStore(): CarbonReportAllocationStore {
    return MemoryCarbonReportAllocationStore(
        mutableMapOf<String, CarbonReportAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportAllocationStore(
    var inner: CarbonReportAllocationStore,
    var cache: TtlCache<CarbonReportAllocation>,
    var hits: Int,
    var misses: Int,
) : CarbonReportAllocationStore {
    override fun find(code: String): CarbonReportAllocation? {
        val hit = (cache.get(code) as CarbonReportAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportAllocation> = inner.list()
}

fun newCachedCarbonReportAllocationStore(inner: CarbonReportAllocationStore, ttl: Int, clock: Clock): CarbonReportAllocationStore {
    return CachedCarbonReportAllocationStore(
        inner = inner,
        cache = newTtlCache<CarbonReportAllocation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report allocations. Empty fields match all. */
data class CarbonReportAllocationQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportAllocation): Boolean {
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

    fun apply(items: MutableList<CarbonReportAllocation>): MutableList<CarbonReportAllocation> {
        val matched = filter(items) { candidate: CarbonReportAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportAllocations(store: CarbonReportAllocationStore, query: CarbonReportAllocationQuery): MutableList<CarbonReportAllocation> {
    return query.apply(store.list())
}
