package com.acme.transport.customs.declaration.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional declaration audits by code. */
interface RegionalDeclarationAuditStore {
    fun find(code: String): RegionalDeclarationAudit?
    fun save(item: RegionalDeclarationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDeclarationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDeclarationAuditStore(
    var items: MutableMap<String, RegionalDeclarationAudit>,
    var order: MutableList<String>,
) : RegionalDeclarationAuditStore {
    override fun find(code: String): RegionalDeclarationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDeclarationAudit) {
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

    override fun list(): MutableList<RegionalDeclarationAudit> {
        val out = mutableListOf<RegionalDeclarationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDeclarationAuditStore(): RegionalDeclarationAuditStore {
    return MemoryRegionalDeclarationAuditStore(
        mutableMapOf<String, RegionalDeclarationAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDeclarationAuditStore(
    var inner: RegionalDeclarationAuditStore,
    var cache: TtlCache<RegionalDeclarationAudit>,
    var hits: Int,
    var misses: Int,
) : RegionalDeclarationAuditStore {
    override fun find(code: String): RegionalDeclarationAudit? {
        val hit = (cache.get(code) as RegionalDeclarationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDeclarationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDeclarationAudit> = inner.list()
}

fun newCachedRegionalDeclarationAuditStore(inner: RegionalDeclarationAuditStore, ttl: Int, clock: Clock): RegionalDeclarationAuditStore {
    return CachedRegionalDeclarationAuditStore(
        inner = inner,
        cache = newTtlCache<RegionalDeclarationAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional declaration audits. Empty fields match all. */
data class RegionalDeclarationAuditQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDeclarationAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDeclarationAudit>): MutableList<RegionalDeclarationAudit> {
        val matched = filter(items) { candidate: RegionalDeclarationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDeclarationAudits(store: RegionalDeclarationAuditStore, query: RegionalDeclarationAuditQuery): MutableList<RegionalDeclarationAudit> {
    return query.apply(store.list())
}
