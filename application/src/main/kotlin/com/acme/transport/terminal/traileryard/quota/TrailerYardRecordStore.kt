package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard records by code. */
interface TrailerYardRecordStore {
    fun find(code: String): TrailerYardRecord?
    fun save(item: TrailerYardRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardRecordStore(
    var items: MutableMap<String, TrailerYardRecord>,
    var order: MutableList<String>,
) : TrailerYardRecordStore {
    override fun find(code: String): TrailerYardRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardRecord) {
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

    override fun list(): MutableList<TrailerYardRecord> {
        val out = mutableListOf<TrailerYardRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardRecordStore(): TrailerYardRecordStore {
    return MemoryTrailerYardRecordStore(mutableMapOf<String, TrailerYardRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardRecordStore(
    var inner: TrailerYardRecordStore,
    var cache: TtlCache<TrailerYardRecord>,
    var hits: Int,
    var misses: Int,
) : TrailerYardRecordStore {
    override fun find(code: String): TrailerYardRecord? {
        val hit = (cache.get(code) as TrailerYardRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardRecord> = inner.list()
}

fun newCachedTrailerYardRecordStore(inner: TrailerYardRecordStore, ttl: Int, clock: Clock): TrailerYardRecordStore {
    return CachedTrailerYardRecordStore(
        inner = inner,
        cache = newTtlCache<TrailerYardRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard records. Empty fields match all. */
data class TrailerYardRecordQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardRecord): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardRecord>): MutableList<TrailerYardRecord> {
        val matched = filter(items) { candidate: TrailerYardRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardRecords(store: TrailerYardRecordStore, query: TrailerYardRecordQuery): MutableList<TrailerYardRecord> {
    return query.apply(store.list())
}
