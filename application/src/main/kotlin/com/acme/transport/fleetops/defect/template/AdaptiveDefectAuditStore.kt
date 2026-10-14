package com.acme.transport.fleetops.defect.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive defect audits by code. */
interface AdaptiveDefectAuditStore {
    fun find(code: String): AdaptiveDefectAudit?
    fun save(item: AdaptiveDefectAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDefectAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDefectAuditStore(
    var items: MutableMap<String, AdaptiveDefectAudit>,
    var order: MutableList<String>,
) : AdaptiveDefectAuditStore {
    override fun find(code: String): AdaptiveDefectAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDefectAudit) {
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

    override fun list(): MutableList<AdaptiveDefectAudit> {
        val out = mutableListOf<AdaptiveDefectAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDefectAuditStore(): AdaptiveDefectAuditStore {
    return MemoryAdaptiveDefectAuditStore(mutableMapOf<String, AdaptiveDefectAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDefectAuditStore(
    var inner: AdaptiveDefectAuditStore,
    var cache: TtlCache<AdaptiveDefectAudit>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDefectAuditStore {
    override fun find(code: String): AdaptiveDefectAudit? {
        val hit = (cache.get(code) as AdaptiveDefectAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDefectAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDefectAudit> = inner.list()
}

fun newCachedAdaptiveDefectAuditStore(inner: AdaptiveDefectAuditStore, ttl: Int, clock: Clock): AdaptiveDefectAuditStore {
    return CachedAdaptiveDefectAuditStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDefectAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive defect audits. Empty fields match all. */
data class AdaptiveDefectAuditQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDefectAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDefectAudit>): MutableList<AdaptiveDefectAudit> {
        val matched = filter(items) { candidate: AdaptiveDefectAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDefectAudits(store: AdaptiveDefectAuditStore, query: AdaptiveDefectAuditQuery): MutableList<AdaptiveDefectAudit> {
    return query.apply(store.list())
}
