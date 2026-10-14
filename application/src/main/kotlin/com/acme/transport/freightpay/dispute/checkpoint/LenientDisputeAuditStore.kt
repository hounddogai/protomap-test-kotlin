package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient dispute audits by code. */
interface LenientDisputeAuditStore {
    fun find(code: String): LenientDisputeAudit?
    fun save(item: LenientDisputeAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDisputeAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDisputeAuditStore(
    var items: MutableMap<String, LenientDisputeAudit>,
    var order: MutableList<String>,
) : LenientDisputeAuditStore {
    override fun find(code: String): LenientDisputeAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDisputeAudit) {
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

    override fun list(): MutableList<LenientDisputeAudit> {
        val out = mutableListOf<LenientDisputeAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDisputeAuditStore(): LenientDisputeAuditStore {
    return MemoryLenientDisputeAuditStore(mutableMapOf<String, LenientDisputeAudit>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDisputeAuditStore(
    var inner: LenientDisputeAuditStore,
    var cache: TtlCache<LenientDisputeAudit>,
    var hits: Int,
    var misses: Int,
) : LenientDisputeAuditStore {
    override fun find(code: String): LenientDisputeAudit? {
        val hit = (cache.get(code) as LenientDisputeAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDisputeAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDisputeAudit> = inner.list()
}

fun newCachedLenientDisputeAuditStore(inner: LenientDisputeAuditStore, ttl: Int, clock: Clock): LenientDisputeAuditStore {
    return CachedLenientDisputeAuditStore(
        inner = inner,
        cache = newTtlCache<LenientDisputeAudit>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dispute audits. Empty fields match all. */
data class LenientDisputeAuditQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDisputeAudit): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LenientDisputeAudit>): MutableList<LenientDisputeAudit> {
        val matched = filter(items) { candidate: LenientDisputeAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDisputeAudits(store: LenientDisputeAuditStore, query: LenientDisputeAuditQuery): MutableList<LenientDisputeAudit> {
    return query.apply(store.list())
}
