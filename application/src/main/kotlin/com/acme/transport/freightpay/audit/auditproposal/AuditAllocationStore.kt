package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit allocations by code. */
interface AuditAllocationStore {
    fun find(code: String): AuditAllocation?
    fun save(item: AuditAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditAllocationStore(
    var items: MutableMap<String, AuditAllocation>,
    var order: MutableList<String>,
) : AuditAllocationStore {
    override fun find(code: String): AuditAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditAllocation) {
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

    override fun list(): MutableList<AuditAllocation> {
        val out = mutableListOf<AuditAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditAllocationStore(): AuditAllocationStore {
    return MemoryAuditAllocationStore(mutableMapOf<String, AuditAllocation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditAllocationStore(
    var inner: AuditAllocationStore,
    var cache: TtlCache<AuditAllocation>,
    var hits: Int,
    var misses: Int,
) : AuditAllocationStore {
    override fun find(code: String): AuditAllocation? {
        val hit = (cache.get(code) as AuditAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditAllocation> = inner.list()
}

fun newCachedAuditAllocationStore(inner: AuditAllocationStore, ttl: Int, clock: Clock): AuditAllocationStore {
    return CachedAuditAllocationStore(
        inner = inner,
        cache = newTtlCache<AuditAllocation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit allocations. Empty fields match all. */
data class AuditAllocationQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditAllocation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditAllocation>): MutableList<AuditAllocation> {
        val matched = filter(items) { candidate: AuditAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditAllocations(store: AuditAllocationStore, query: AuditAllocationQuery): MutableList<AuditAllocation> {
    return query.apply(store.list())
}
