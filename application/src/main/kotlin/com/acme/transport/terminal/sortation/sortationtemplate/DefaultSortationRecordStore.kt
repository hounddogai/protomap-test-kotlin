package com.acme.transport.terminal.sortation.sortationtemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default sortation records by code. */
interface DefaultSortationRecordStore {
    fun find(code: String): DefaultSortationRecord?
    fun save(item: DefaultSortationRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultSortationRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultSortationRecordStore(
    var items: MutableMap<String, DefaultSortationRecord>,
    var order: MutableList<String>,
) : DefaultSortationRecordStore {
    override fun find(code: String): DefaultSortationRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultSortationRecord) {
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

    override fun list(): MutableList<DefaultSortationRecord> {
        val out = mutableListOf<DefaultSortationRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultSortationRecordStore(): DefaultSortationRecordStore {
    return MemoryDefaultSortationRecordStore(
        mutableMapOf<String, DefaultSortationRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultSortationRecordStore(
    var inner: DefaultSortationRecordStore,
    var cache: TtlCache<DefaultSortationRecord>,
    var hits: Int,
    var misses: Int,
) : DefaultSortationRecordStore {
    override fun find(code: String): DefaultSortationRecord? {
        val hit = (cache.get(code) as DefaultSortationRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultSortationRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultSortationRecord> = inner.list()
}

fun newCachedDefaultSortationRecordStore(inner: DefaultSortationRecordStore, ttl: Int, clock: Clock): DefaultSortationRecordStore {
    return CachedDefaultSortationRecordStore(
        inner = inner,
        cache = newTtlCache<DefaultSortationRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default sortation records. Empty fields match all. */
data class DefaultSortationRecordQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultSortationRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultSortationRecord>): MutableList<DefaultSortationRecord> {
        val matched = filter(items) { candidate: DefaultSortationRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultSortationRecords(store: DefaultSortationRecordStore, query: DefaultSortationRecordQuery): MutableList<DefaultSortationRecord> {
    return query.apply(store.list())
}
