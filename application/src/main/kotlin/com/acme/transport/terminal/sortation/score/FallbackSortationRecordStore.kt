package com.acme.transport.terminal.sortation.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps fallback sortation records by code. */
interface FallbackSortationRecordStore {
    fun find(code: String): FallbackSortationRecord?
    fun save(item: FallbackSortationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackSortationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackSortationRecordStore(
    var items: MutableMap<String, FallbackSortationRecord>,
    var order: MutableList<String>,
) : FallbackSortationRecordStore {
    override fun find(code: String): FallbackSortationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackSortationRecord) {
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

    override fun list(): MutableList<FallbackSortationRecord> {
        val out = mutableListOf<FallbackSortationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackSortationRecordStore(): FallbackSortationRecordStore {
    return MemoryFallbackSortationRecordStore(
        mutableMapOf<String, FallbackSortationRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackSortationRecordStore(
    var inner: FallbackSortationRecordStore,
    var cache: TtlCache<FallbackSortationRecord>,
    var hits: Int,
    var misses: Int,
) : FallbackSortationRecordStore {
    override fun find(code: String): FallbackSortationRecord? {
        val hit = (cache.get(code) as FallbackSortationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackSortationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackSortationRecord> = inner.list()
}

fun newCachedFallbackSortationRecordStore(inner: FallbackSortationRecordStore, ttl: Int, clock: Clock): FallbackSortationRecordStore {
    return CachedFallbackSortationRecordStore(
        inner = inner,
        cache = newTtlCache<FallbackSortationRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback sortation records. Empty fields match all. */
data class FallbackSortationRecordQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackSortationRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackSortationRecord>): MutableList<FallbackSortationRecord> {
        val matched = filter(items) { candidate: FallbackSortationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackSortationRecords(store: FallbackSortationRecordStore, query: FallbackSortationRecordQuery): MutableList<FallbackSortationRecord> {
    return query.apply(store.list())
}
