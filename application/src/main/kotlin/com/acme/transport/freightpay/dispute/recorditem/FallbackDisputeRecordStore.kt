package com.acme.transport.freightpay.dispute.recorditem

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback dispute records by code. */
interface FallbackDisputeRecordStore {
    fun find(code: String): FallbackDisputeRecord?
    fun save(item: FallbackDisputeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDisputeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDisputeRecordStore(
    var items: MutableMap<String, FallbackDisputeRecord>,
    var order: MutableList<String>,
) : FallbackDisputeRecordStore {
    override fun find(code: String): FallbackDisputeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDisputeRecord) {
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

    override fun list(): MutableList<FallbackDisputeRecord> {
        val out = mutableListOf<FallbackDisputeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDisputeRecordStore(): FallbackDisputeRecordStore {
    return MemoryFallbackDisputeRecordStore(
        mutableMapOf<String, FallbackDisputeRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDisputeRecordStore(
    var inner: FallbackDisputeRecordStore,
    var cache: TtlCache<FallbackDisputeRecord>,
    var hits: Int,
    var misses: Int,
) : FallbackDisputeRecordStore {
    override fun find(code: String): FallbackDisputeRecord? {
        val hit = (cache.get(code) as FallbackDisputeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDisputeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDisputeRecord> = inner.list()
}

fun newCachedFallbackDisputeRecordStore(inner: FallbackDisputeRecordStore, ttl: Int, clock: Clock): FallbackDisputeRecordStore {
    return CachedFallbackDisputeRecordStore(
        inner = inner,
        cache = newTtlCache<FallbackDisputeRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback dispute records. Empty fields match all. */
data class FallbackDisputeRecordQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDisputeRecord): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackDisputeRecord>): MutableList<FallbackDisputeRecord> {
        val matched = filter(items) { candidate: FallbackDisputeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDisputeRecords(store: FallbackDisputeRecordStore, query: FallbackDisputeRecordQuery): MutableList<FallbackDisputeRecord> {
    return query.apply(store.list())
}
