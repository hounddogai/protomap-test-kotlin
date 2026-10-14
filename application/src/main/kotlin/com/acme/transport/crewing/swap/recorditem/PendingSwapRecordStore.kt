package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending swap records by code. */
interface PendingSwapRecordStore {
    fun find(code: String): PendingSwapRecord?
    fun save(item: PendingSwapRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingSwapRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingSwapRecordStore(
    var items: MutableMap<String, PendingSwapRecord>,
    var order: MutableList<String>,
) : PendingSwapRecordStore {
    override fun find(code: String): PendingSwapRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingSwapRecord) {
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

    override fun list(): MutableList<PendingSwapRecord> {
        val out = mutableListOf<PendingSwapRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingSwapRecordStore(): PendingSwapRecordStore {
    return MemoryPendingSwapRecordStore(mutableMapOf<String, PendingSwapRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingSwapRecordStore(
    var inner: PendingSwapRecordStore,
    var cache: TtlCache<PendingSwapRecord>,
    var hits: Int,
    var misses: Int,
) : PendingSwapRecordStore {
    override fun find(code: String): PendingSwapRecord? {
        val hit = (cache.get(code) as PendingSwapRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingSwapRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingSwapRecord> = inner.list()
}

fun newCachedPendingSwapRecordStore(inner: PendingSwapRecordStore, ttl: Int, clock: Clock): PendingSwapRecordStore {
    return CachedPendingSwapRecordStore(
        inner = inner,
        cache = newTtlCache<PendingSwapRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending swap records. Empty fields match all. */
data class PendingSwapRecordQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingSwapRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingSwapRecord>): MutableList<PendingSwapRecord> {
        val matched = filter(items) { candidate: PendingSwapRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingSwapRecords(store: PendingSwapRecordStore, query: PendingSwapRecordQuery): MutableList<PendingSwapRecord> {
    return query.apply(store.list())
}
