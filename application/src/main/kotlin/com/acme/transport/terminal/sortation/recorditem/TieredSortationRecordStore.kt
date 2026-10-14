package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered sortation records by code. */
interface TieredSortationRecordStore {
    fun find(code: String): TieredSortationRecord?
    fun save(item: TieredSortationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSortationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSortationRecordStore(
    var items: MutableMap<String, TieredSortationRecord>,
    var order: MutableList<String>,
) : TieredSortationRecordStore {
    override fun find(code: String): TieredSortationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSortationRecord) {
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

    override fun list(): MutableList<TieredSortationRecord> {
        val out = mutableListOf<TieredSortationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSortationRecordStore(): TieredSortationRecordStore {
    return MemoryTieredSortationRecordStore(
        mutableMapOf<String, TieredSortationRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSortationRecordStore(
    var inner: TieredSortationRecordStore,
    var cache: TtlCache<TieredSortationRecord>,
    var hits: Int,
    var misses: Int,
) : TieredSortationRecordStore {
    override fun find(code: String): TieredSortationRecord? {
        val hit = (cache.get(code) as TieredSortationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSortationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSortationRecord> = inner.list()
}

fun newCachedTieredSortationRecordStore(inner: TieredSortationRecordStore, ttl: Int, clock: Clock): TieredSortationRecordStore {
    return CachedTieredSortationRecordStore(
        inner = inner,
        cache = newTtlCache<TieredSortationRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered sortation records. Empty fields match all. */
data class TieredSortationRecordQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSortationRecord): Boolean {
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

    fun apply(items: MutableList<TieredSortationRecord>): MutableList<TieredSortationRecord> {
        val matched = filter(items) { candidate: TieredSortationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSortationRecords(store: TieredSortationRecordStore, query: TieredSortationRecordQuery): MutableList<TieredSortationRecord> {
    return query.apply(store.list())
}
