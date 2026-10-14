package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps scheduled tender records by code. */
interface ScheduledTenderRecordStore {
    fun find(code: String): ScheduledTenderRecord?
    fun save(item: ScheduledTenderRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledTenderRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledTenderRecordStore(
    var items: MutableMap<String, ScheduledTenderRecord>,
    var order: MutableList<String>,
) : ScheduledTenderRecordStore {
    override fun find(code: String): ScheduledTenderRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledTenderRecord) {
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

    override fun list(): MutableList<ScheduledTenderRecord> {
        val out = mutableListOf<ScheduledTenderRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledTenderRecordStore(): ScheduledTenderRecordStore {
    return MemoryScheduledTenderRecordStore(
        mutableMapOf<String, ScheduledTenderRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledTenderRecordStore(
    var inner: ScheduledTenderRecordStore,
    var cache: TtlCache<ScheduledTenderRecord>,
    var hits: Int,
    var misses: Int,
) : ScheduledTenderRecordStore {
    override fun find(code: String): ScheduledTenderRecord? {
        val hit = (cache.get(code) as ScheduledTenderRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledTenderRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledTenderRecord> = inner.list()
}

fun newCachedScheduledTenderRecordStore(inner: ScheduledTenderRecordStore, ttl: Int, clock: Clock): ScheduledTenderRecordStore {
    return CachedScheduledTenderRecordStore(
        inner = inner,
        cache = newTtlCache<ScheduledTenderRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled tender records. Empty fields match all. */
data class ScheduledTenderRecordQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledTenderRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledTenderRecord>): MutableList<ScheduledTenderRecord> {
        val matched = filter(items) { candidate: ScheduledTenderRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledTenderRecords(store: ScheduledTenderRecordStore, query: ScheduledTenderRecordQuery): MutableList<ScheduledTenderRecord> {
    return query.apply(store.list())
}
