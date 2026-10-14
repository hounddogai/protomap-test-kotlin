package com.acme.transport.fleetops.defect.notice

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback defect records by code. */
interface FallbackDefectRecordStore {
    fun find(code: String): FallbackDefectRecord?
    fun save(item: FallbackDefectRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDefectRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDefectRecordStore(
    var items: MutableMap<String, FallbackDefectRecord>,
    var order: MutableList<String>,
) : FallbackDefectRecordStore {
    override fun find(code: String): FallbackDefectRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDefectRecord) {
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

    override fun list(): MutableList<FallbackDefectRecord> {
        val out = mutableListOf<FallbackDefectRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDefectRecordStore(): FallbackDefectRecordStore {
    return MemoryFallbackDefectRecordStore(
        mutableMapOf<String, FallbackDefectRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDefectRecordStore(
    var inner: FallbackDefectRecordStore,
    var cache: TtlCache<FallbackDefectRecord>,
    var hits: Int,
    var misses: Int,
) : FallbackDefectRecordStore {
    override fun find(code: String): FallbackDefectRecord? {
        val hit = (cache.get(code) as FallbackDefectRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDefectRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDefectRecord> = inner.list()
}

fun newCachedFallbackDefectRecordStore(inner: FallbackDefectRecordStore, ttl: Int, clock: Clock): FallbackDefectRecordStore {
    return CachedFallbackDefectRecordStore(
        inner = inner,
        cache = newTtlCache<FallbackDefectRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback defect records. Empty fields match all. */
data class FallbackDefectRecordQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDefectRecord): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDefectRecord>): MutableList<FallbackDefectRecord> {
        val matched = filter(items) { candidate: FallbackDefectRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDefectRecords(store: FallbackDefectRecordStore, query: FallbackDefectRecordQuery): MutableList<FallbackDefectRecord> {
    return query.apply(store.list())
}
