package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched trailer entries by code. */
interface BatchedTrailerEntryStore {
    fun find(code: String): BatchedTrailerEntry?
    fun save(item: BatchedTrailerEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerEntryStore(
    var items: MutableMap<String, BatchedTrailerEntry>,
    var order: MutableList<String>,
) : BatchedTrailerEntryStore {
    override fun find(code: String): BatchedTrailerEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerEntry) {
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

    override fun list(): MutableList<BatchedTrailerEntry> {
        val out = mutableListOf<BatchedTrailerEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerEntryStore(): BatchedTrailerEntryStore {
    return MemoryBatchedTrailerEntryStore(mutableMapOf<String, BatchedTrailerEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerEntryStore(
    var inner: BatchedTrailerEntryStore,
    var cache: TtlCache<BatchedTrailerEntry>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerEntryStore {
    override fun find(code: String): BatchedTrailerEntry? {
        val hit = (cache.get(code) as BatchedTrailerEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerEntry> = inner.list()
}

fun newCachedBatchedTrailerEntryStore(inner: BatchedTrailerEntryStore, ttl: Int, clock: Clock): BatchedTrailerEntryStore {
    return CachedBatchedTrailerEntryStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer entries. Empty fields match all. */
data class BatchedTrailerEntryQuery(
    var country: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerEntry): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<BatchedTrailerEntry>): MutableList<BatchedTrailerEntry> {
        val matched = filter(items) { candidate: BatchedTrailerEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerEntries(store: BatchedTrailerEntryStore, query: BatchedTrailerEntryQuery): MutableList<BatchedTrailerEntry> {
    return query.apply(store.list())
}
