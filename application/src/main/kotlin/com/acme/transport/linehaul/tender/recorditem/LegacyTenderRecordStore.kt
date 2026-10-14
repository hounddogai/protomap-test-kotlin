package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy tender records by code. */
interface LegacyTenderRecordStore {
    fun find(code: String): LegacyTenderRecord?
    fun save(item: LegacyTenderRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTenderRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTenderRecordStore(
    var items: MutableMap<String, LegacyTenderRecord>,
    var order: MutableList<String>,
) : LegacyTenderRecordStore {
    override fun find(code: String): LegacyTenderRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTenderRecord) {
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

    override fun list(): MutableList<LegacyTenderRecord> {
        val out = mutableListOf<LegacyTenderRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTenderRecordStore(): LegacyTenderRecordStore {
    return MemoryLegacyTenderRecordStore(mutableMapOf<String, LegacyTenderRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTenderRecordStore(
    var inner: LegacyTenderRecordStore,
    var cache: TtlCache<LegacyTenderRecord>,
    var hits: Int,
    var misses: Int,
) : LegacyTenderRecordStore {
    override fun find(code: String): LegacyTenderRecord? {
        val hit = (cache.get(code) as LegacyTenderRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTenderRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTenderRecord> = inner.list()
}

fun newCachedLegacyTenderRecordStore(inner: LegacyTenderRecordStore, ttl: Int, clock: Clock): LegacyTenderRecordStore {
    return CachedLegacyTenderRecordStore(
        inner = inner,
        cache = newTtlCache<LegacyTenderRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy tender records. Empty fields match all. */
data class LegacyTenderRecordQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTenderRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<LegacyTenderRecord>): MutableList<LegacyTenderRecord> {
        val matched = filter(items) { candidate: LegacyTenderRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTenderRecords(store: LegacyTenderRecordStore, query: LegacyTenderRecordQuery): MutableList<LegacyTenderRecord> {
    return query.apply(store.list())
}
