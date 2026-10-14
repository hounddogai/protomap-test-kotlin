package com.acme.transport.crewing.availability.score

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability records by code. */
interface AvailabilityRecordStore {
    fun find(code: String): AvailabilityRecord?
    fun save(item: AvailabilityRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityRecordStore(
    var items: MutableMap<String, AvailabilityRecord>,
    var order: MutableList<String>,
) : AvailabilityRecordStore {
    override fun find(code: String): AvailabilityRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityRecord) {
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

    override fun list(): MutableList<AvailabilityRecord> {
        val out = mutableListOf<AvailabilityRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityRecordStore(): AvailabilityRecordStore {
    return MemoryAvailabilityRecordStore(mutableMapOf<String, AvailabilityRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityRecordStore(
    var inner: AvailabilityRecordStore,
    var cache: TtlCache<AvailabilityRecord>,
    var hits: Int,
    var misses: Int,
) : AvailabilityRecordStore {
    override fun find(code: String): AvailabilityRecord? {
        val hit = (cache.get(code) as AvailabilityRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityRecord> = inner.list()
}

fun newCachedAvailabilityRecordStore(inner: AvailabilityRecordStore, ttl: Int, clock: Clock): AvailabilityRecordStore {
    return CachedAvailabilityRecordStore(
        inner = inner,
        cache = newTtlCache<AvailabilityRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability records. Empty fields match all. */
data class AvailabilityRecordQuery(
    var title: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityRecord>): MutableList<AvailabilityRecord> {
        val matched = filter(items) { candidate: AvailabilityRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityRecords(store: AvailabilityRecordStore, query: AvailabilityRecordQuery): MutableList<AvailabilityRecord> {
    return query.apply(store.list())
}
