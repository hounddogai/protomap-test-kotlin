package com.acme.transport.freightpay.audit.auditrule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient audit records by code. */
interface LenientAuditRecordStore {
    fun find(code: String): LenientAuditRecord?
    fun save(item: LenientAuditRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAuditRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAuditRecordStore(
    var items: MutableMap<String, LenientAuditRecord>,
    var order: MutableList<String>,
) : LenientAuditRecordStore {
    override fun find(code: String): LenientAuditRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAuditRecord) {
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

    override fun list(): MutableList<LenientAuditRecord> {
        val out = mutableListOf<LenientAuditRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAuditRecordStore(): LenientAuditRecordStore {
    return MemoryLenientAuditRecordStore(mutableMapOf<String, LenientAuditRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAuditRecordStore(
    var inner: LenientAuditRecordStore,
    var cache: TtlCache<LenientAuditRecord>,
    var hits: Int,
    var misses: Int,
) : LenientAuditRecordStore {
    override fun find(code: String): LenientAuditRecord? {
        val hit = (cache.get(code) as LenientAuditRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAuditRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAuditRecord> = inner.list()
}

fun newCachedLenientAuditRecordStore(inner: LenientAuditRecordStore, ttl: Int, clock: Clock): LenientAuditRecordStore {
    return CachedLenientAuditRecordStore(
        inner = inner,
        cache = newTtlCache<LenientAuditRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient audit records. Empty fields match all. */
data class LenientAuditRecordQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAuditRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAuditRecord>): MutableList<LenientAuditRecord> {
        val matched = filter(items) { candidate: LenientAuditRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAuditRecords(store: LenientAuditRecordStore, query: LenientAuditRecordQuery): MutableList<LenientAuditRecord> {
    return query.apply(store.list())
}
