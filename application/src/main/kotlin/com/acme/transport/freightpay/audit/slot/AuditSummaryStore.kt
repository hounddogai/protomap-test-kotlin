package com.acme.transport.freightpay.audit.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit summaries by code. */
interface AuditSummaryStore {
    fun find(code: String): AuditSummary?
    fun save(item: AuditSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditSummaryStore(
    var items: MutableMap<String, AuditSummary>,
    var order: MutableList<String>,
) : AuditSummaryStore {
    override fun find(code: String): AuditSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditSummary) {
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

    override fun list(): MutableList<AuditSummary> {
        val out = mutableListOf<AuditSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditSummaryStore(): AuditSummaryStore {
    return MemoryAuditSummaryStore(mutableMapOf<String, AuditSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditSummaryStore(
    var inner: AuditSummaryStore,
    var cache: TtlCache<AuditSummary>,
    var hits: Int,
    var misses: Int,
) : AuditSummaryStore {
    override fun find(code: String): AuditSummary? {
        val hit = (cache.get(code) as AuditSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditSummary> = inner.list()
}

fun newCachedAuditSummaryStore(inner: AuditSummaryStore, ttl: Int, clock: Clock): AuditSummaryStore {
    return CachedAuditSummaryStore(
        inner = inner,
        cache = newTtlCache<AuditSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit summaries. Empty fields match all. */
data class AuditSummaryQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditSummary>): MutableList<AuditSummary> {
        val matched = filter(items) { candidate: AuditSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditSummaries(store: AuditSummaryStore, query: AuditSummaryQuery): MutableList<AuditSummary> {
    return query.apply(store.list())
}
