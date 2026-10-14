package com.acme.transport.customs.declaration.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration audits by code. */
interface DeclarationAuditStore {
    fun find(code: String): DeclarationAudit?
    fun save(item: DeclarationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationAuditStore(
    var items: MutableMap<String, DeclarationAudit>,
    var order: MutableList<String>,
) : DeclarationAuditStore {
    override fun find(code: String): DeclarationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationAudit) {
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

    override fun list(): MutableList<DeclarationAudit> {
        val out = mutableListOf<DeclarationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationAuditStore(): DeclarationAuditStore {
    return MemoryDeclarationAuditStore(mutableMapOf<String, DeclarationAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationAuditStore(
    var inner: DeclarationAuditStore,
    var cache: TtlCache<DeclarationAudit>,
    var hits: Int,
    var misses: Int,
) : DeclarationAuditStore {
    override fun find(code: String): DeclarationAudit? {
        val hit = (cache.get(code) as DeclarationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationAudit> = inner.list()
}

fun newCachedDeclarationAuditStore(inner: DeclarationAuditStore, ttl: Int, clock: Clock): DeclarationAuditStore {
    return CachedDeclarationAuditStore(
        inner = inner,
        cache = newTtlCache<DeclarationAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration audits. Empty fields match all. */
data class DeclarationAuditQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DeclarationAudit>): MutableList<DeclarationAudit> {
        val matched = filter(items) { candidate: DeclarationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationAudits(store: DeclarationAuditStore, query: DeclarationAuditQuery): MutableList<DeclarationAudit> {
    return query.apply(store.list())
}
