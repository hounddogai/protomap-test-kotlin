package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive cutoff records by code. */
interface AdaptiveCutoffRecordStore {
    fun find(code: String): AdaptiveCutoffRecord?
    fun save(item: AdaptiveCutoffRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCutoffRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCutoffRecordStore(
    var items: MutableMap<String, AdaptiveCutoffRecord>,
    var order: MutableList<String>,
) : AdaptiveCutoffRecordStore {
    override fun find(code: String): AdaptiveCutoffRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCutoffRecord) {
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

    override fun list(): MutableList<AdaptiveCutoffRecord> {
        val out = mutableListOf<AdaptiveCutoffRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCutoffRecordStore(): AdaptiveCutoffRecordStore {
    return MemoryAdaptiveCutoffRecordStore(
        mutableMapOf<String, AdaptiveCutoffRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCutoffRecordStore(
    var inner: AdaptiveCutoffRecordStore,
    var cache: TtlCache<AdaptiveCutoffRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCutoffRecordStore {
    override fun find(code: String): AdaptiveCutoffRecord? {
        val hit = (cache.get(code) as AdaptiveCutoffRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCutoffRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCutoffRecord> = inner.list()
}

fun newCachedAdaptiveCutoffRecordStore(inner: AdaptiveCutoffRecordStore, ttl: Int, clock: Clock): AdaptiveCutoffRecordStore {
    return CachedAdaptiveCutoffRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCutoffRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive cutoff records. Empty fields match all. */
data class AdaptiveCutoffRecordQuery(
    var country: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCutoffRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCutoffRecord>): MutableList<AdaptiveCutoffRecord> {
        val matched = filter(items) { candidate: AdaptiveCutoffRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCutoffRecords(store: AdaptiveCutoffRecordStore, query: AdaptiveCutoffRecordQuery): MutableList<AdaptiveCutoffRecord> {
    return query.apply(store.list())
}
