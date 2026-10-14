package com.acme.transport.freightpay.audit.recorditem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending audit records by code. */
interface PendingAuditRecordStore {
    fun find(code: String): PendingAuditRecord?
    fun save(item: PendingAuditRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAuditRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAuditRecordStore(
    var items: MutableMap<String, PendingAuditRecord>,
    var order: MutableList<String>,
) : PendingAuditRecordStore {
    override fun find(code: String): PendingAuditRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAuditRecord) {
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

    override fun list(): MutableList<PendingAuditRecord> {
        val out = mutableListOf<PendingAuditRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAuditRecordStore(): PendingAuditRecordStore {
    return MemoryPendingAuditRecordStore(mutableMapOf<String, PendingAuditRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAuditRecordStore(
    var inner: PendingAuditRecordStore,
    var cache: TtlCache<PendingAuditRecord>,
    var hits: Int,
    var misses: Int,
) : PendingAuditRecordStore {
    override fun find(code: String): PendingAuditRecord? {
        val hit = (cache.get(code) as PendingAuditRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAuditRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAuditRecord> = inner.list()
}

fun newCachedPendingAuditRecordStore(inner: PendingAuditRecordStore, ttl: Int, clock: Clock): PendingAuditRecordStore {
    return CachedPendingAuditRecordStore(
        inner = inner,
        cache = newTtlCache<PendingAuditRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending audit records. Empty fields match all. */
data class PendingAuditRecordQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAuditRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingAuditRecord>): MutableList<PendingAuditRecord> {
        val matched = filter(items) { candidate: PendingAuditRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAuditRecords(store: PendingAuditRecordStore, query: PendingAuditRecordQuery): MutableList<PendingAuditRecord> {
    return query.apply(store.list())
}
