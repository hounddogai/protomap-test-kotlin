package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel burn records by code. */
interface RegionalFuelBurnRecordStore {
    fun find(code: String): RegionalFuelBurnRecord?
    fun save(item: RegionalFuelBurnRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelBurnRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelBurnRecordStore(
    var items: MutableMap<String, RegionalFuelBurnRecord>,
    var order: MutableList<String>,
) : RegionalFuelBurnRecordStore {
    override fun find(code: String): RegionalFuelBurnRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelBurnRecord) {
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

    override fun list(): MutableList<RegionalFuelBurnRecord> {
        val out = mutableListOf<RegionalFuelBurnRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelBurnRecordStore(): RegionalFuelBurnRecordStore {
    return MemoryRegionalFuelBurnRecordStore(
        mutableMapOf<String, RegionalFuelBurnRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelBurnRecordStore(
    var inner: RegionalFuelBurnRecordStore,
    var cache: TtlCache<RegionalFuelBurnRecord>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelBurnRecordStore {
    override fun find(code: String): RegionalFuelBurnRecord? {
        val hit = (cache.get(code) as RegionalFuelBurnRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelBurnRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelBurnRecord> = inner.list()
}

fun newCachedRegionalFuelBurnRecordStore(inner: RegionalFuelBurnRecordStore, ttl: Int, clock: Clock): RegionalFuelBurnRecordStore {
    return CachedRegionalFuelBurnRecordStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelBurnRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel burn records. Empty fields match all. */
data class RegionalFuelBurnRecordQuery(
    var country: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelBurnRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RegionalFuelBurnRecord>): MutableList<RegionalFuelBurnRecord> {
        val matched = filter(items) { candidate: RegionalFuelBurnRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelBurnRecords(store: RegionalFuelBurnRecordStore, query: RegionalFuelBurnRecordQuery): MutableList<RegionalFuelBurnRecord> {
    return query.apply(store.list())
}
