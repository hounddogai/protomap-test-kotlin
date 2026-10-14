package com.acme.transport.fleetops.tire.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal tire records by code. */
interface SeasonalTireRecordStore {
    fun find(code: String): SeasonalTireRecord?
    fun save(item: SeasonalTireRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTireRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTireRecordStore(
    var items: MutableMap<String, SeasonalTireRecord>,
    var order: MutableList<String>,
) : SeasonalTireRecordStore {
    override fun find(code: String): SeasonalTireRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTireRecord) {
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

    override fun list(): MutableList<SeasonalTireRecord> {
        val out = mutableListOf<SeasonalTireRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTireRecordStore(): SeasonalTireRecordStore {
    return MemorySeasonalTireRecordStore(mutableMapOf<String, SeasonalTireRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTireRecordStore(
    var inner: SeasonalTireRecordStore,
    var cache: TtlCache<SeasonalTireRecord>,
    var hits: Int,
    var misses: Int,
) : SeasonalTireRecordStore {
    override fun find(code: String): SeasonalTireRecord? {
        val hit = (cache.get(code) as SeasonalTireRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTireRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTireRecord> = inner.list()
}

fun newCachedSeasonalTireRecordStore(inner: SeasonalTireRecordStore, ttl: Int, clock: Clock): SeasonalTireRecordStore {
    return CachedSeasonalTireRecordStore(
        inner = inner,
        cache = newTtlCache<SeasonalTireRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal tire records. Empty fields match all. */
data class SeasonalTireRecordQuery(
    var category: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTireRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTireRecord>): MutableList<SeasonalTireRecord> {
        val matched = filter(items) { candidate: SeasonalTireRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTireRecords(store: SeasonalTireRecordStore, query: SeasonalTireRecordQuery): MutableList<SeasonalTireRecord> {
    return query.apply(store.list())
}
