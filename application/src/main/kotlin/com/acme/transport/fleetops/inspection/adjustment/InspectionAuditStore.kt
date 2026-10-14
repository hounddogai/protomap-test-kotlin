package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection audits by code. */
interface InspectionAuditStore {
    fun find(code: String): InspectionAudit?
    fun save(item: InspectionAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionAuditStore(
    var items: MutableMap<String, InspectionAudit>,
    var order: MutableList<String>,
) : InspectionAuditStore {
    override fun find(code: String): InspectionAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionAudit) {
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

    override fun list(): MutableList<InspectionAudit> {
        val out = mutableListOf<InspectionAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionAuditStore(): InspectionAuditStore {
    return MemoryInspectionAuditStore(mutableMapOf<String, InspectionAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionAuditStore(
    var inner: InspectionAuditStore,
    var cache: TtlCache<InspectionAudit>,
    var hits: Int,
    var misses: Int,
) : InspectionAuditStore {
    override fun find(code: String): InspectionAudit? {
        val hit = (cache.get(code) as InspectionAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionAudit> = inner.list()
}

fun newCachedInspectionAuditStore(inner: InspectionAuditStore, ttl: Int, clock: Clock): InspectionAuditStore {
    return CachedInspectionAuditStore(
        inner = inner,
        cache = newTtlCache<InspectionAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection audits. Empty fields match all. */
data class InspectionAuditQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionAudit>): MutableList<InspectionAudit> {
        val matched = filter(items) { candidate: InspectionAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionAudits(store: InspectionAuditStore, query: InspectionAuditQuery): MutableList<InspectionAudit> {
    return query.apply(store.list())
}
