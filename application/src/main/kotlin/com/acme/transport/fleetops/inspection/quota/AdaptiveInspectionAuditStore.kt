package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection audits by code. */
interface AdaptiveInspectionAuditStore {
    fun find(code: String): AdaptiveInspectionAudit?
    fun save(item: AdaptiveInspectionAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionAuditStore(
    var items: MutableMap<String, AdaptiveInspectionAudit>,
    var order: MutableList<String>,
) : AdaptiveInspectionAuditStore {
    override fun find(code: String): AdaptiveInspectionAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionAudit) {
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

    override fun list(): MutableList<AdaptiveInspectionAudit> {
        val out = mutableListOf<AdaptiveInspectionAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionAuditStore(): AdaptiveInspectionAuditStore {
    return MemoryAdaptiveInspectionAuditStore(
        mutableMapOf<String, AdaptiveInspectionAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionAuditStore(
    var inner: AdaptiveInspectionAuditStore,
    var cache: TtlCache<AdaptiveInspectionAudit>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionAuditStore {
    override fun find(code: String): AdaptiveInspectionAudit? {
        val hit = (cache.get(code) as AdaptiveInspectionAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionAudit> = inner.list()
}

fun newCachedAdaptiveInspectionAuditStore(inner: AdaptiveInspectionAuditStore, ttl: Int, clock: Clock): AdaptiveInspectionAuditStore {
    return CachedAdaptiveInspectionAuditStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionAudit>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection audits. Empty fields match all. */
data class AdaptiveInspectionAuditQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionAudit): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionAudit>): MutableList<AdaptiveInspectionAudit> {
        val matched = filter(items) { candidate: AdaptiveInspectionAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionAudits(store: AdaptiveInspectionAuditStore, query: AdaptiveInspectionAuditQuery): MutableList<AdaptiveInspectionAudit> {
    return query.apply(store.list())
}
