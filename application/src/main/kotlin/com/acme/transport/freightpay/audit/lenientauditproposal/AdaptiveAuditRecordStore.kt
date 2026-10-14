package com.acme.transport.freightpay.audit.lenientauditproposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive audit records by code. */
interface AdaptiveAuditRecordStore {
    fun find(code: String): AdaptiveAuditRecord?
    fun save(item: AdaptiveAuditRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveAuditRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveAuditRecordStore(
    var items: MutableMap<String, AdaptiveAuditRecord>,
    var order: MutableList<String>,
) : AdaptiveAuditRecordStore {
    override fun find(code: String): AdaptiveAuditRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveAuditRecord) {
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

    override fun list(): MutableList<AdaptiveAuditRecord> {
        val out = mutableListOf<AdaptiveAuditRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveAuditRecordStore(): AdaptiveAuditRecordStore {
    return MemoryAdaptiveAuditRecordStore(mutableMapOf<String, AdaptiveAuditRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveAuditRecordStore(
    var inner: AdaptiveAuditRecordStore,
    var cache: TtlCache<AdaptiveAuditRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveAuditRecordStore {
    override fun find(code: String): AdaptiveAuditRecord? {
        val hit = (cache.get(code) as AdaptiveAuditRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveAuditRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveAuditRecord> = inner.list()
}

fun newCachedAdaptiveAuditRecordStore(inner: AdaptiveAuditRecordStore, ttl: Int, clock: Clock): AdaptiveAuditRecordStore {
    return CachedAdaptiveAuditRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveAuditRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive audit records. Empty fields match all. */
data class AdaptiveAuditRecordQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveAuditRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveAuditRecord>): MutableList<AdaptiveAuditRecord> {
        val matched = filter(items) { candidate: AdaptiveAuditRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveAuditRecords(store: AdaptiveAuditRecordStore, query: AdaptiveAuditRecordQuery): MutableList<AdaptiveAuditRecord> {
    return query.apply(store.list())
}
