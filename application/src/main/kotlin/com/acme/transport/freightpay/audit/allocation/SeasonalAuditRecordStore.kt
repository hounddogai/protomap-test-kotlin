package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal audit records by code. */
interface SeasonalAuditRecordStore {
    fun find(code: String): SeasonalAuditRecord?
    fun save(item: SeasonalAuditRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalAuditRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalAuditRecordStore(
    var items: MutableMap<String, SeasonalAuditRecord>,
    var order: MutableList<String>,
) : SeasonalAuditRecordStore {
    override fun find(code: String): SeasonalAuditRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalAuditRecord) {
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

    override fun list(): MutableList<SeasonalAuditRecord> {
        val out = mutableListOf<SeasonalAuditRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalAuditRecordStore(): SeasonalAuditRecordStore {
    return MemorySeasonalAuditRecordStore(mutableMapOf<String, SeasonalAuditRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalAuditRecordStore(
    var inner: SeasonalAuditRecordStore,
    var cache: TtlCache<SeasonalAuditRecord>,
    var hits: Int,
    var misses: Int,
) : SeasonalAuditRecordStore {
    override fun find(code: String): SeasonalAuditRecord? {
        val hit = (cache.get(code) as SeasonalAuditRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalAuditRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalAuditRecord> = inner.list()
}

fun newCachedSeasonalAuditRecordStore(inner: SeasonalAuditRecordStore, ttl: Int, clock: Clock): SeasonalAuditRecordStore {
    return CachedSeasonalAuditRecordStore(
        inner = inner,
        cache = newTtlCache<SeasonalAuditRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal audit records. Empty fields match all. */
data class SeasonalAuditRecordQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalAuditRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalAuditRecord>): MutableList<SeasonalAuditRecord> {
        val matched = filter(items) { candidate: SeasonalAuditRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalAuditRecords(store: SeasonalAuditRecordStore, query: SeasonalAuditRecordQuery): MutableList<SeasonalAuditRecord> {
    return query.apply(store.list())
}
