package com.acme.transport.network.transittime.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time records by code. */
interface TransitTimeRecordStore {
    fun find(code: String): TransitTimeRecord?
    fun save(item: TransitTimeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeRecordStore(
    var items: MutableMap<String, TransitTimeRecord>,
    var order: MutableList<String>,
) : TransitTimeRecordStore {
    override fun find(code: String): TransitTimeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeRecord) {
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

    override fun list(): MutableList<TransitTimeRecord> {
        val out = mutableListOf<TransitTimeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeRecordStore(): TransitTimeRecordStore {
    return MemoryTransitTimeRecordStore(mutableMapOf<String, TransitTimeRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeRecordStore(
    var inner: TransitTimeRecordStore,
    var cache: TtlCache<TransitTimeRecord>,
    var hits: Int,
    var misses: Int,
) : TransitTimeRecordStore {
    override fun find(code: String): TransitTimeRecord? {
        val hit = (cache.get(code) as TransitTimeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeRecord> = inner.list()
}

fun newCachedTransitTimeRecordStore(inner: TransitTimeRecordStore, ttl: Int, clock: Clock): TransitTimeRecordStore {
    return CachedTransitTimeRecordStore(
        inner = inner,
        cache = newTtlCache<TransitTimeRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time records. Empty fields match all. */
data class TransitTimeRecordQuery(
    var channel: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeRecord): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeRecord>): MutableList<TransitTimeRecord> {
        val matched = filter(items) { candidate: TransitTimeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeRecords(store: TransitTimeRecordStore, query: TransitTimeRecordQuery): MutableList<TransitTimeRecord> {
    return query.apply(store.list())
}
