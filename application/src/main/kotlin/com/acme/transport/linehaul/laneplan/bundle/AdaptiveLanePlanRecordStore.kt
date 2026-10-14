package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive lane plan records by code. */
interface AdaptiveLanePlanRecordStore {
    fun find(code: String): AdaptiveLanePlanRecord?
    fun save(item: AdaptiveLanePlanRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveLanePlanRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveLanePlanRecordStore(
    var items: MutableMap<String, AdaptiveLanePlanRecord>,
    var order: MutableList<String>,
) : AdaptiveLanePlanRecordStore {
    override fun find(code: String): AdaptiveLanePlanRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveLanePlanRecord) {
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

    override fun list(): MutableList<AdaptiveLanePlanRecord> {
        val out = mutableListOf<AdaptiveLanePlanRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveLanePlanRecordStore(): AdaptiveLanePlanRecordStore {
    return MemoryAdaptiveLanePlanRecordStore(
        mutableMapOf<String, AdaptiveLanePlanRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveLanePlanRecordStore(
    var inner: AdaptiveLanePlanRecordStore,
    var cache: TtlCache<AdaptiveLanePlanRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveLanePlanRecordStore {
    override fun find(code: String): AdaptiveLanePlanRecord? {
        val hit = (cache.get(code) as AdaptiveLanePlanRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveLanePlanRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveLanePlanRecord> = inner.list()
}

fun newCachedAdaptiveLanePlanRecordStore(inner: AdaptiveLanePlanRecordStore, ttl: Int, clock: Clock): AdaptiveLanePlanRecordStore {
    return CachedAdaptiveLanePlanRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveLanePlanRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive lane plan records. Empty fields match all. */
data class AdaptiveLanePlanRecordQuery(
    var locale: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveLanePlanRecord): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveLanePlanRecord>): MutableList<AdaptiveLanePlanRecord> {
        val matched = filter(items) { candidate: AdaptiveLanePlanRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveLanePlanRecords(store: AdaptiveLanePlanRecordStore, query: AdaptiveLanePlanRecordQuery): MutableList<AdaptiveLanePlanRecord> {
    return query.apply(store.list())
}
