package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period records by code. */
interface DutyPeriodRecordStore {
    fun find(code: String): DutyPeriodRecord?
    fun save(item: DutyPeriodRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodRecordStore(
    var items: MutableMap<String, DutyPeriodRecord>,
    var order: MutableList<String>,
) : DutyPeriodRecordStore {
    override fun find(code: String): DutyPeriodRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodRecord) {
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

    override fun list(): MutableList<DutyPeriodRecord> {
        val out = mutableListOf<DutyPeriodRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodRecordStore(): DutyPeriodRecordStore {
    return MemoryDutyPeriodRecordStore(mutableMapOf<String, DutyPeriodRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodRecordStore(
    var inner: DutyPeriodRecordStore,
    var cache: TtlCache<DutyPeriodRecord>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodRecordStore {
    override fun find(code: String): DutyPeriodRecord? {
        val hit = (cache.get(code) as DutyPeriodRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodRecord> = inner.list()
}

fun newCachedDutyPeriodRecordStore(inner: DutyPeriodRecordStore, ttl: Int, clock: Clock): DutyPeriodRecordStore {
    return CachedDutyPeriodRecordStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period records. Empty fields match all. */
data class DutyPeriodRecordQuery(
    var category: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<DutyPeriodRecord>): MutableList<DutyPeriodRecord> {
        val matched = filter(items) { candidate: DutyPeriodRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodRecords(store: DutyPeriodRecordStore, query: DutyPeriodRecordQuery): MutableList<DutyPeriodRecord> {
    return query.apply(store.list())
}
