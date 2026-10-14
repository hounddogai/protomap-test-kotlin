package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps global cross dock audits by code. */
interface GlobalCrossDockAuditStore {
    fun find(code: String): GlobalCrossDockAudit?
    fun save(item: GlobalCrossDockAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCrossDockAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCrossDockAuditStore(
    var items: MutableMap<String, GlobalCrossDockAudit>,
    var order: MutableList<String>,
) : GlobalCrossDockAuditStore {
    override fun find(code: String): GlobalCrossDockAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCrossDockAudit) {
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

    override fun list(): MutableList<GlobalCrossDockAudit> {
        val out = mutableListOf<GlobalCrossDockAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCrossDockAuditStore(): GlobalCrossDockAuditStore {
    return MemoryGlobalCrossDockAuditStore(
        mutableMapOf<String, GlobalCrossDockAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCrossDockAuditStore(
    var inner: GlobalCrossDockAuditStore,
    var cache: TtlCache<GlobalCrossDockAudit>,
    var hits: Int,
    var misses: Int,
) : GlobalCrossDockAuditStore {
    override fun find(code: String): GlobalCrossDockAudit? {
        val hit = (cache.get(code) as GlobalCrossDockAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCrossDockAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCrossDockAudit> = inner.list()
}

fun newCachedGlobalCrossDockAuditStore(inner: GlobalCrossDockAuditStore, ttl: Int, clock: Clock): GlobalCrossDockAuditStore {
    return CachedGlobalCrossDockAuditStore(
        inner = inner,
        cache = newTtlCache<GlobalCrossDockAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cross dock audits. Empty fields match all. */
data class GlobalCrossDockAuditQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCrossDockAudit): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCrossDockAudit>): MutableList<GlobalCrossDockAudit> {
        val matched = filter(items) { candidate: GlobalCrossDockAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCrossDockAudits(store: GlobalCrossDockAuditStore, query: GlobalCrossDockAuditQuery): MutableList<GlobalCrossDockAudit> {
    return query.apply(store.list())
}
