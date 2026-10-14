package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive availability audits by code. */
interface AdaptiveAvailabilityAuditStore {
    fun find(code: String): AdaptiveAvailabilityAudit?
    fun save(item: AdaptiveAvailabilityAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAvailabilityAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAvailabilityAuditStore(
    var items: MutableMap<String, AdaptiveAvailabilityAudit>,
    var order: MutableList<String>,
) : AdaptiveAvailabilityAuditStore {
    override fun find(code: String): AdaptiveAvailabilityAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAvailabilityAudit) {
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

    override fun list(): MutableList<AdaptiveAvailabilityAudit> {
        val out = mutableListOf<AdaptiveAvailabilityAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAvailabilityAuditStore(): AdaptiveAvailabilityAuditStore {
    return MemoryAdaptiveAvailabilityAuditStore(
        mutableMapOf<String, AdaptiveAvailabilityAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAvailabilityAuditStore(
    var inner: AdaptiveAvailabilityAuditStore,
    var cache: TtlCache<AdaptiveAvailabilityAudit>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAvailabilityAuditStore {
    override fun find(code: String): AdaptiveAvailabilityAudit? {
        val hit = (cache.get(code) as AdaptiveAvailabilityAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAvailabilityAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAvailabilityAudit> = inner.list()
}

fun newCachedAdaptiveAvailabilityAuditStore(inner: AdaptiveAvailabilityAuditStore, ttl: Int, clock: Clock): AdaptiveAvailabilityAuditStore {
    return CachedAdaptiveAvailabilityAuditStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAvailabilityAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive availability audits. Empty fields match all. */
data class AdaptiveAvailabilityAuditQuery(
    var region: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAvailabilityAudit): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<AdaptiveAvailabilityAudit>): MutableList<AdaptiveAvailabilityAudit> {
        val matched = filter(items) { candidate: AdaptiveAvailabilityAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAvailabilityAudits(store: AdaptiveAvailabilityAuditStore, query: AdaptiveAvailabilityAuditQuery): MutableList<AdaptiveAvailabilityAudit> {
    return query.apply(store.list())
}
