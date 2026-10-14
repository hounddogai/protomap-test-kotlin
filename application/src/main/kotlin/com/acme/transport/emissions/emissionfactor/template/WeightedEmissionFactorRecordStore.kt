package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted emission factor records by code. */
interface WeightedEmissionFactorRecordStore {
    fun find(code: String): WeightedEmissionFactorRecord?
    fun save(item: WeightedEmissionFactorRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEmissionFactorRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEmissionFactorRecordStore(
    var items: MutableMap<String, WeightedEmissionFactorRecord>,
    var order: MutableList<String>,
) : WeightedEmissionFactorRecordStore {
    override fun find(code: String): WeightedEmissionFactorRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEmissionFactorRecord) {
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

    override fun list(): MutableList<WeightedEmissionFactorRecord> {
        val out = mutableListOf<WeightedEmissionFactorRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEmissionFactorRecordStore(): WeightedEmissionFactorRecordStore {
    return MemoryWeightedEmissionFactorRecordStore(
        mutableMapOf<String, WeightedEmissionFactorRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEmissionFactorRecordStore(
    var inner: WeightedEmissionFactorRecordStore,
    var cache: TtlCache<WeightedEmissionFactorRecord>,
    var hits: Int,
    var misses: Int,
) : WeightedEmissionFactorRecordStore {
    override fun find(code: String): WeightedEmissionFactorRecord? {
        val hit = (cache.get(code) as WeightedEmissionFactorRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEmissionFactorRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEmissionFactorRecord> = inner.list()
}

fun newCachedWeightedEmissionFactorRecordStore(inner: WeightedEmissionFactorRecordStore, ttl: Int, clock: Clock): WeightedEmissionFactorRecordStore {
    return CachedWeightedEmissionFactorRecordStore(
        inner = inner,
        cache = newTtlCache<WeightedEmissionFactorRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted emission factor records. Empty fields match all. */
data class WeightedEmissionFactorRecordQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEmissionFactorRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedEmissionFactorRecord>): MutableList<WeightedEmissionFactorRecord> {
        val matched = filter(items) { candidate: WeightedEmissionFactorRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEmissionFactorRecords(store: WeightedEmissionFactorRecordStore, query: WeightedEmissionFactorRecordQuery): MutableList<WeightedEmissionFactorRecord> {
    return query.apply(store.list())
}
