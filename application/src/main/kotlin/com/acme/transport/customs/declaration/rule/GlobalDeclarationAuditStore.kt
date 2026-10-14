package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global declaration audits by code. */
interface GlobalDeclarationAuditStore {
    fun find(code: String): GlobalDeclarationAudit?
    fun save(item: GlobalDeclarationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalDeclarationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalDeclarationAuditStore(
    var items: MutableMap<String, GlobalDeclarationAudit>,
    var order: MutableList<String>,
) : GlobalDeclarationAuditStore {
    override fun find(code: String): GlobalDeclarationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalDeclarationAudit) {
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

    override fun list(): MutableList<GlobalDeclarationAudit> {
        val out = mutableListOf<GlobalDeclarationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalDeclarationAuditStore(): GlobalDeclarationAuditStore {
    return MemoryGlobalDeclarationAuditStore(
        mutableMapOf<String, GlobalDeclarationAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalDeclarationAuditStore(
    var inner: GlobalDeclarationAuditStore,
    var cache: TtlCache<GlobalDeclarationAudit>,
    var hits: Int,
    var misses: Int,
) : GlobalDeclarationAuditStore {
    override fun find(code: String): GlobalDeclarationAudit? {
        val hit = (cache.get(code) as GlobalDeclarationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalDeclarationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalDeclarationAudit> = inner.list()
}

fun newCachedGlobalDeclarationAuditStore(inner: GlobalDeclarationAuditStore, ttl: Int, clock: Clock): GlobalDeclarationAuditStore {
    return CachedGlobalDeclarationAuditStore(
        inner = inner,
        cache = newTtlCache<GlobalDeclarationAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global declaration audits. Empty fields match all. */
data class GlobalDeclarationAuditQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalDeclarationAudit): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalDeclarationAudit>): MutableList<GlobalDeclarationAudit> {
        val matched = filter(items) { candidate: GlobalDeclarationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalDeclarationAudits(store: GlobalDeclarationAuditStore, query: GlobalDeclarationAuditQuery): MutableList<GlobalDeclarationAudit> {
    return query.apply(store.list())
}
