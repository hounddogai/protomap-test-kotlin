package com.acme.transport.crewing.qualification.qualificationproposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending qualification audits by code. */
interface PendingQualificationAuditStore {
    fun find(code: String): PendingQualificationAudit?
    fun save(item: PendingQualificationAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingQualificationAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingQualificationAuditStore(
    var items: MutableMap<String, PendingQualificationAudit>,
    var order: MutableList<String>,
) : PendingQualificationAuditStore {
    override fun find(code: String): PendingQualificationAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingQualificationAudit) {
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

    override fun list(): MutableList<PendingQualificationAudit> {
        val out = mutableListOf<PendingQualificationAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingQualificationAuditStore(): PendingQualificationAuditStore {
    return MemoryPendingQualificationAuditStore(
        mutableMapOf<String, PendingQualificationAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingQualificationAuditStore(
    var inner: PendingQualificationAuditStore,
    var cache: TtlCache<PendingQualificationAudit>,
    var hits: Int,
    var misses: Int,
) : PendingQualificationAuditStore {
    override fun find(code: String): PendingQualificationAudit? {
        val hit = (cache.get(code) as PendingQualificationAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingQualificationAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingQualificationAudit> = inner.list()
}

fun newCachedPendingQualificationAuditStore(inner: PendingQualificationAuditStore, ttl: Int, clock: Clock): PendingQualificationAuditStore {
    return CachedPendingQualificationAuditStore(
        inner = inner,
        cache = newTtlCache<PendingQualificationAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending qualification audits. Empty fields match all. */
data class PendingQualificationAuditQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingQualificationAudit): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingQualificationAudit>): MutableList<PendingQualificationAudit> {
        val matched = filter(items) { candidate: PendingQualificationAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingQualificationAudits(store: PendingQualificationAuditStore, query: PendingQualificationAuditQuery): MutableList<PendingQualificationAudit> {
    return query.apply(store.list())
}
