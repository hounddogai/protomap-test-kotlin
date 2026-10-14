package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery audits by code. */
interface RedeliveryAuditStore {
    fun find(code: String): RedeliveryAudit?
    fun save(item: RedeliveryAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryAuditStore(
    var items: MutableMap<String, RedeliveryAudit>,
    var order: MutableList<String>,
) : RedeliveryAuditStore {
    override fun find(code: String): RedeliveryAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryAudit) {
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

    override fun list(): MutableList<RedeliveryAudit> {
        val out = mutableListOf<RedeliveryAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryAuditStore(): RedeliveryAuditStore {
    return MemoryRedeliveryAuditStore(mutableMapOf<String, RedeliveryAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryAuditStore(
    var inner: RedeliveryAuditStore,
    var cache: TtlCache<RedeliveryAudit>,
    var hits: Int,
    var misses: Int,
) : RedeliveryAuditStore {
    override fun find(code: String): RedeliveryAudit? {
        val hit = (cache.get(code) as RedeliveryAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryAudit> = inner.list()
}

fun newCachedRedeliveryAuditStore(inner: RedeliveryAuditStore, ttl: Int, clock: Clock): RedeliveryAuditStore {
    return CachedRedeliveryAuditStore(
        inner = inner,
        cache = newTtlCache<RedeliveryAudit>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery audits. Empty fields match all. */
data class RedeliveryAuditQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryAudit>): MutableList<RedeliveryAudit> {
        val matched = filter(items) { candidate: RedeliveryAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryAudits(store: RedeliveryAuditStore, query: RedeliveryAuditQuery): MutableList<RedeliveryAudit> {
    return query.apply(store.list())
}
