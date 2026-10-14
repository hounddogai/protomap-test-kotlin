package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn records by code. */
interface FuelBurnRecordStore {
    fun find(code: String): FuelBurnRecord?
    fun save(item: FuelBurnRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnRecordStore(
    var items: MutableMap<String, FuelBurnRecord>,
    var order: MutableList<String>,
) : FuelBurnRecordStore {
    override fun find(code: String): FuelBurnRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnRecord) {
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

    override fun list(): MutableList<FuelBurnRecord> {
        val out = mutableListOf<FuelBurnRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnRecordStore(): FuelBurnRecordStore {
    return MemoryFuelBurnRecordStore(mutableMapOf<String, FuelBurnRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnRecordStore(
    var inner: FuelBurnRecordStore,
    var cache: TtlCache<FuelBurnRecord>,
    var hits: Int,
    var misses: Int,
) : FuelBurnRecordStore {
    override fun find(code: String): FuelBurnRecord? {
        val hit = (cache.get(code) as FuelBurnRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnRecord> = inner.list()
}

fun newCachedFuelBurnRecordStore(inner: FuelBurnRecordStore, ttl: Int, clock: Clock): FuelBurnRecordStore {
    return CachedFuelBurnRecordStore(
        inner = inner,
        cache = newTtlCache<FuelBurnRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn records. Empty fields match all. */
data class FuelBurnRecordQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnRecord>): MutableList<FuelBurnRecord> {
        val matched = filter(items) { candidate: FuelBurnRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnRecords(store: FuelBurnRecordStore, query: FuelBurnRecordQuery): MutableList<FuelBurnRecord> {
    return query.apply(store.list())
}
