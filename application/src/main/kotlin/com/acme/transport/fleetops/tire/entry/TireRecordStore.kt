package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire records by code. */
interface TireRecordStore {
    fun find(code: String): TireRecord?
    fun save(item: TireRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireRecordStore(
    var items: MutableMap<String, TireRecord>,
    var order: MutableList<String>,
) : TireRecordStore {
    override fun find(code: String): TireRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireRecord) {
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

    override fun list(): MutableList<TireRecord> {
        val out = mutableListOf<TireRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireRecordStore(): TireRecordStore {
    return MemoryTireRecordStore(mutableMapOf<String, TireRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireRecordStore(
    var inner: TireRecordStore,
    var cache: TtlCache<TireRecord>,
    var hits: Int,
    var misses: Int,
) : TireRecordStore {
    override fun find(code: String): TireRecord? {
        val hit = (cache.get(code) as TireRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireRecord> = inner.list()
}

fun newCachedTireRecordStore(inner: TireRecordStore, ttl: Int, clock: Clock): TireRecordStore {
    return CachedTireRecordStore(
        inner = inner,
        cache = newTtlCache<TireRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire records. Empty fields match all. */
data class TireRecordQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireRecord): Boolean {
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

    fun apply(items: MutableList<TireRecord>): MutableList<TireRecord> {
        val matched = filter(items) { candidate: TireRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireRecords(store: TireRecordStore, query: TireRecordQuery): MutableList<TireRecord> {
    return query.apply(store.list())
}
