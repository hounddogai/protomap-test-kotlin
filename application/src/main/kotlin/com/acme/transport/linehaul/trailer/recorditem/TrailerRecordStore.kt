package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer records by code. */
interface TrailerRecordStore {
    fun find(code: String): TrailerRecord?
    fun save(item: TrailerRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerRecordStore(
    var items: MutableMap<String, TrailerRecord>,
    var order: MutableList<String>,
) : TrailerRecordStore {
    override fun find(code: String): TrailerRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerRecord) {
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

    override fun list(): MutableList<TrailerRecord> {
        val out = mutableListOf<TrailerRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerRecordStore(): TrailerRecordStore {
    return MemoryTrailerRecordStore(mutableMapOf<String, TrailerRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerRecordStore(
    var inner: TrailerRecordStore,
    var cache: TtlCache<TrailerRecord>,
    var hits: Int,
    var misses: Int,
) : TrailerRecordStore {
    override fun find(code: String): TrailerRecord? {
        val hit = (cache.get(code) as TrailerRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerRecord> = inner.list()
}

fun newCachedTrailerRecordStore(inner: TrailerRecordStore, ttl: Int, clock: Clock): TrailerRecordStore {
    return CachedTrailerRecordStore(
        inner = inner,
        cache = newTtlCache<TrailerRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer records. Empty fields match all. */
data class TrailerRecordQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerRecord>): MutableList<TrailerRecord> {
        val matched = filter(items) { candidate: TrailerRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerRecords(store: TrailerRecordStore, query: TrailerRecordQuery): MutableList<TrailerRecord> {
    return query.apply(store.list())
}
