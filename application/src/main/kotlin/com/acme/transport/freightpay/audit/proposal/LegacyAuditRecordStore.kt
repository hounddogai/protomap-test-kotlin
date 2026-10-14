package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy audit records by code. */
interface LegacyAuditRecordStore {
    fun find(code: String): LegacyAuditRecord?
    fun save(item: LegacyAuditRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAuditRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAuditRecordStore(
    var items: MutableMap<String, LegacyAuditRecord>,
    var order: MutableList<String>,
) : LegacyAuditRecordStore {
    override fun find(code: String): LegacyAuditRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAuditRecord) {
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

    override fun list(): MutableList<LegacyAuditRecord> {
        val out = mutableListOf<LegacyAuditRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAuditRecordStore(): LegacyAuditRecordStore {
    return MemoryLegacyAuditRecordStore(mutableMapOf<String, LegacyAuditRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAuditRecordStore(
    var inner: LegacyAuditRecordStore,
    var cache: TtlCache<LegacyAuditRecord>,
    var hits: Int,
    var misses: Int,
) : LegacyAuditRecordStore {
    override fun find(code: String): LegacyAuditRecord? {
        val hit = (cache.get(code) as LegacyAuditRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAuditRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAuditRecord> = inner.list()
}

fun newCachedLegacyAuditRecordStore(inner: LegacyAuditRecordStore, ttl: Int, clock: Clock): LegacyAuditRecordStore {
    return CachedLegacyAuditRecordStore(
        inner = inner,
        cache = newTtlCache<LegacyAuditRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy audit records. Empty fields match all. */
data class LegacyAuditRecordQuery(
    var segment: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAuditRecord): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyAuditRecord>): MutableList<LegacyAuditRecord> {
        val matched = filter(items) { candidate: LegacyAuditRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAuditRecords(store: LegacyAuditRecordStore, query: LegacyAuditRecordQuery): MutableList<LegacyAuditRecord> {
    return query.apply(store.list())
}
