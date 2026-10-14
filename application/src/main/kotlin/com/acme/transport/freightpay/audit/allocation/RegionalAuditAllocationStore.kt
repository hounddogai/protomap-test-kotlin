package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional audit allocations by code. */
interface RegionalAuditAllocationStore {
    fun find(code: String): RegionalAuditAllocation?
    fun save(item: RegionalAuditAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAuditAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAuditAllocationStore(
    var items: MutableMap<String, RegionalAuditAllocation>,
    var order: MutableList<String>,
) : RegionalAuditAllocationStore {
    override fun find(code: String): RegionalAuditAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAuditAllocation) {
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

    override fun list(): MutableList<RegionalAuditAllocation> {
        val out = mutableListOf<RegionalAuditAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAuditAllocationStore(): RegionalAuditAllocationStore {
    return MemoryRegionalAuditAllocationStore(
        mutableMapOf<String, RegionalAuditAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAuditAllocationStore(
    var inner: RegionalAuditAllocationStore,
    var cache: TtlCache<RegionalAuditAllocation>,
    var hits: Int,
    var misses: Int,
) : RegionalAuditAllocationStore {
    override fun find(code: String): RegionalAuditAllocation? {
        val hit = (cache.get(code) as RegionalAuditAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAuditAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAuditAllocation> = inner.list()
}

fun newCachedRegionalAuditAllocationStore(inner: RegionalAuditAllocationStore, ttl: Int, clock: Clock): RegionalAuditAllocationStore {
    return CachedRegionalAuditAllocationStore(
        inner = inner,
        cache = newTtlCache<RegionalAuditAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional audit allocations. Empty fields match all. */
data class RegionalAuditAllocationQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAuditAllocation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAuditAllocation>): MutableList<RegionalAuditAllocation> {
        val matched = filter(items) { candidate: RegionalAuditAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAuditAllocations(store: RegionalAuditAllocationStore, query: RegionalAuditAllocationQuery): MutableList<RegionalAuditAllocation> {
    return query.apply(store.list())
}
