package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity records by code. */
interface CapacityRecordStore {
    fun find(code: String): CapacityRecord?
    fun save(item: CapacityRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityRecordStore(
    var items: MutableMap<String, CapacityRecord>,
    var order: MutableList<String>,
) : CapacityRecordStore {
    override fun find(code: String): CapacityRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityRecord) {
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

    override fun list(): MutableList<CapacityRecord> {
        val out = mutableListOf<CapacityRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityRecordStore(): CapacityRecordStore {
    return MemoryCapacityRecordStore(mutableMapOf<String, CapacityRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityRecordStore(
    var inner: CapacityRecordStore,
    var cache: TtlCache<CapacityRecord>,
    var hits: Int,
    var misses: Int,
) : CapacityRecordStore {
    override fun find(code: String): CapacityRecord? {
        val hit = (cache.get(code) as CapacityRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityRecord> = inner.list()
}

fun newCachedCapacityRecordStore(inner: CapacityRecordStore, ttl: Int, clock: Clock): CapacityRecordStore {
    return CachedCapacityRecordStore(
        inner = inner,
        cache = newTtlCache<CapacityRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity records. Empty fields match all. */
data class CapacityRecordQuery(
    var source: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityRecord>): MutableList<CapacityRecord> {
        val matched = filter(items) { candidate: CapacityRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityRecords(store: CapacityRecordStore, query: CapacityRecordQuery): MutableList<CapacityRecord> {
    return query.apply(store.list())
}
