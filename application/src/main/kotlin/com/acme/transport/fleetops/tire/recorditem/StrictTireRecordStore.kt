package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict tire records by code. */
interface StrictTireRecordStore {
    fun find(code: String): StrictTireRecord?
    fun save(item: StrictTireRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTireRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTireRecordStore(
    var items: MutableMap<String, StrictTireRecord>,
    var order: MutableList<String>,
) : StrictTireRecordStore {
    override fun find(code: String): StrictTireRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTireRecord) {
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

    override fun list(): MutableList<StrictTireRecord> {
        val out = mutableListOf<StrictTireRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTireRecordStore(): StrictTireRecordStore {
    return MemoryStrictTireRecordStore(mutableMapOf<String, StrictTireRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTireRecordStore(
    var inner: StrictTireRecordStore,
    var cache: TtlCache<StrictTireRecord>,
    var hits: Int,
    var misses: Int,
) : StrictTireRecordStore {
    override fun find(code: String): StrictTireRecord? {
        val hit = (cache.get(code) as StrictTireRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTireRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTireRecord> = inner.list()
}

fun newCachedStrictTireRecordStore(inner: StrictTireRecordStore, ttl: Int, clock: Clock): StrictTireRecordStore {
    return CachedStrictTireRecordStore(
        inner = inner,
        cache = newTtlCache<StrictTireRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tire records. Empty fields match all. */
data class StrictTireRecordQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTireRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTireRecord>): MutableList<StrictTireRecord> {
        val matched = filter(items) { candidate: StrictTireRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTireRecords(store: StrictTireRecordStore, query: StrictTireRecordQuery): MutableList<StrictTireRecord> {
    return query.apply(store.list())
}
