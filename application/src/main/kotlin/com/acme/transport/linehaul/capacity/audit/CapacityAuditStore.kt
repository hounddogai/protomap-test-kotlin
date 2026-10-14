package com.acme.transport.linehaul.capacity.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity audits by code. */
interface CapacityAuditStore {
    fun find(code: String): CapacityAudit?
    fun save(item: CapacityAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityAuditStore(
    var items: MutableMap<String, CapacityAudit>,
    var order: MutableList<String>,
) : CapacityAuditStore {
    override fun find(code: String): CapacityAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityAudit) {
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

    override fun list(): MutableList<CapacityAudit> {
        val out = mutableListOf<CapacityAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityAuditStore(): CapacityAuditStore {
    return MemoryCapacityAuditStore(mutableMapOf<String, CapacityAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityAuditStore(
    var inner: CapacityAuditStore,
    var cache: TtlCache<CapacityAudit>,
    var hits: Int,
    var misses: Int,
) : CapacityAuditStore {
    override fun find(code: String): CapacityAudit? {
        val hit = (cache.get(code) as CapacityAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityAudit> = inner.list()
}

fun newCachedCapacityAuditStore(inner: CapacityAuditStore, ttl: Int, clock: Clock): CapacityAuditStore {
    return CachedCapacityAuditStore(
        inner = inner,
        cache = newTtlCache<CapacityAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity audits. Empty fields match all. */
data class CapacityAuditQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityAudit>): MutableList<CapacityAudit> {
        val matched = filter(items) { candidate: CapacityAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityAudits(store: CapacityAuditStore, query: CapacityAuditQuery): MutableList<CapacityAudit> {
    return query.apply(store.list())
}
