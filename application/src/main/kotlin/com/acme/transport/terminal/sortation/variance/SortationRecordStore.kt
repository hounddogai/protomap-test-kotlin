package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation records by code. */
interface SortationRecordStore {
    fun find(code: String): SortationRecord?
    fun save(item: SortationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationRecordStore(
    var items: MutableMap<String, SortationRecord>,
    var order: MutableList<String>,
) : SortationRecordStore {
    override fun find(code: String): SortationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationRecord) {
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

    override fun list(): MutableList<SortationRecord> {
        val out = mutableListOf<SortationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationRecordStore(): SortationRecordStore {
    return MemorySortationRecordStore(mutableMapOf<String, SortationRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationRecordStore(
    var inner: SortationRecordStore,
    var cache: TtlCache<SortationRecord>,
    var hits: Int,
    var misses: Int,
) : SortationRecordStore {
    override fun find(code: String): SortationRecord? {
        val hit = (cache.get(code) as SortationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationRecord> = inner.list()
}

fun newCachedSortationRecordStore(inner: SortationRecordStore, ttl: Int, clock: Clock): SortationRecordStore {
    return CachedSortationRecordStore(
        inner = inner,
        cache = newTtlCache<SortationRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation records. Empty fields match all. */
data class SortationRecordQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SortationRecord>): MutableList<SortationRecord> {
        val matched = filter(items) { candidate: SortationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationRecords(store: SortationRecordStore, query: SortationRecordQuery): MutableList<SortationRecord> {
    return query.apply(store.list())
}
