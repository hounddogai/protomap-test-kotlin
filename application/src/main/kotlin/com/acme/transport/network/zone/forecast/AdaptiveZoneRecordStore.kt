package com.acme.transport.network.zone.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive zone records by code. */
interface AdaptiveZoneRecordStore {
    fun find(code: String): AdaptiveZoneRecord?
    fun save(item: AdaptiveZoneRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveZoneRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveZoneRecordStore(
    var items: MutableMap<String, AdaptiveZoneRecord>,
    var order: MutableList<String>,
) : AdaptiveZoneRecordStore {
    override fun find(code: String): AdaptiveZoneRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveZoneRecord) {
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

    override fun list(): MutableList<AdaptiveZoneRecord> {
        val out = mutableListOf<AdaptiveZoneRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveZoneRecordStore(): AdaptiveZoneRecordStore {
    return MemoryAdaptiveZoneRecordStore(mutableMapOf<String, AdaptiveZoneRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveZoneRecordStore(
    var inner: AdaptiveZoneRecordStore,
    var cache: TtlCache<AdaptiveZoneRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveZoneRecordStore {
    override fun find(code: String): AdaptiveZoneRecord? {
        val hit = (cache.get(code) as AdaptiveZoneRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveZoneRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveZoneRecord> = inner.list()
}

fun newCachedAdaptiveZoneRecordStore(inner: AdaptiveZoneRecordStore, ttl: Int, clock: Clock): AdaptiveZoneRecordStore {
    return CachedAdaptiveZoneRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveZoneRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive zone records. Empty fields match all. */
data class AdaptiveZoneRecordQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveZoneRecord): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<AdaptiveZoneRecord>): MutableList<AdaptiveZoneRecord> {
        val matched = filter(items) { candidate: AdaptiveZoneRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveZoneRecords(store: AdaptiveZoneRecordStore, query: AdaptiveZoneRecordQuery): MutableList<AdaptiveZoneRecord> {
    return query.apply(store.list())
}
