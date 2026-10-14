package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional inspection audits by code. */
interface RegionalInspectionAuditStore {
    fun find(code: String): RegionalInspectionAudit?
    fun save(item: RegionalInspectionAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalInspectionAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalInspectionAuditStore(
    var items: MutableMap<String, RegionalInspectionAudit>,
    var order: MutableList<String>,
) : RegionalInspectionAuditStore {
    override fun find(code: String): RegionalInspectionAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalInspectionAudit) {
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

    override fun list(): MutableList<RegionalInspectionAudit> {
        val out = mutableListOf<RegionalInspectionAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalInspectionAuditStore(): RegionalInspectionAuditStore {
    return MemoryRegionalInspectionAuditStore(
        mutableMapOf<String, RegionalInspectionAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalInspectionAuditStore(
    var inner: RegionalInspectionAuditStore,
    var cache: TtlCache<RegionalInspectionAudit>,
    var hits: Int,
    var misses: Int,
) : RegionalInspectionAuditStore {
    override fun find(code: String): RegionalInspectionAudit? {
        val hit = (cache.get(code) as RegionalInspectionAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalInspectionAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalInspectionAudit> = inner.list()
}

fun newCachedRegionalInspectionAuditStore(inner: RegionalInspectionAuditStore, ttl: Int, clock: Clock): RegionalInspectionAuditStore {
    return CachedRegionalInspectionAuditStore(
        inner = inner,
        cache = newTtlCache<RegionalInspectionAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional inspection audits. Empty fields match all. */
data class RegionalInspectionAuditQuery(
    var channel: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalInspectionAudit): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalInspectionAudit>): MutableList<RegionalInspectionAudit> {
        val matched = filter(items) { candidate: RegionalInspectionAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalInspectionAudits(store: RegionalInspectionAuditStore, query: RegionalInspectionAuditQuery): MutableList<RegionalInspectionAudit> {
    return query.apply(store.list())
}
