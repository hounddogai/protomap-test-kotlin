package com.acme.transport.customs.clearance.policy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance audits by code. */
interface ClearanceAuditStore {
    fun find(code: String): ClearanceAudit?
    fun save(item: ClearanceAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceAuditStore(
    var items: MutableMap<String, ClearanceAudit>,
    var order: MutableList<String>,
) : ClearanceAuditStore {
    override fun find(code: String): ClearanceAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceAudit) {
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

    override fun list(): MutableList<ClearanceAudit> {
        val out = mutableListOf<ClearanceAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceAuditStore(): ClearanceAuditStore {
    return MemoryClearanceAuditStore(mutableMapOf<String, ClearanceAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceAuditStore(
    var inner: ClearanceAuditStore,
    var cache: TtlCache<ClearanceAudit>,
    var hits: Int,
    var misses: Int,
) : ClearanceAuditStore {
    override fun find(code: String): ClearanceAudit? {
        val hit = (cache.get(code) as ClearanceAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceAudit> = inner.list()
}

fun newCachedClearanceAuditStore(inner: ClearanceAuditStore, ttl: Int, clock: Clock): ClearanceAuditStore {
    return CachedClearanceAuditStore(
        inner = inner,
        cache = newTtlCache<ClearanceAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance audits. Empty fields match all. */
data class ClearanceAuditQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceAudit>): MutableList<ClearanceAudit> {
        val matched = filter(items) { candidate: ClearanceAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceAudits(store: ClearanceAuditStore, query: ClearanceAuditQuery): MutableList<ClearanceAudit> {
    return query.apply(store.list())
}
