package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive camera event records by code. */
interface AdaptiveCameraEventRecordStore {
    fun find(code: String): AdaptiveCameraEventRecord?
    fun save(item: AdaptiveCameraEventRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCameraEventRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCameraEventRecordStore(
    var items: MutableMap<String, AdaptiveCameraEventRecord>,
    var order: MutableList<String>,
) : AdaptiveCameraEventRecordStore {
    override fun find(code: String): AdaptiveCameraEventRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCameraEventRecord) {
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

    override fun list(): MutableList<AdaptiveCameraEventRecord> {
        val out = mutableListOf<AdaptiveCameraEventRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCameraEventRecordStore(): AdaptiveCameraEventRecordStore {
    return MemoryAdaptiveCameraEventRecordStore(
        mutableMapOf<String, AdaptiveCameraEventRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCameraEventRecordStore(
    var inner: AdaptiveCameraEventRecordStore,
    var cache: TtlCache<AdaptiveCameraEventRecord>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCameraEventRecordStore {
    override fun find(code: String): AdaptiveCameraEventRecord? {
        val hit = (cache.get(code) as AdaptiveCameraEventRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCameraEventRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCameraEventRecord> = inner.list()
}

fun newCachedAdaptiveCameraEventRecordStore(inner: AdaptiveCameraEventRecordStore, ttl: Int, clock: Clock): AdaptiveCameraEventRecordStore {
    return CachedAdaptiveCameraEventRecordStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCameraEventRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive camera event records. Empty fields match all. */
data class AdaptiveCameraEventRecordQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCameraEventRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCameraEventRecord>): MutableList<AdaptiveCameraEventRecord> {
        val matched = filter(items) { candidate: AdaptiveCameraEventRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCameraEventRecords(store: AdaptiveCameraEventRecordStore, query: AdaptiveCameraEventRecordQuery): MutableList<AdaptiveCameraEventRecord> {
    return query.apply(store.list())
}
