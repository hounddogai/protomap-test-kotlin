package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict attempt records by code. */
interface StrictAttemptRecordStore {
    fun find(code: String): StrictAttemptRecord?
    fun save(item: StrictAttemptRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAttemptRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAttemptRecordStore(
    var items: MutableMap<String, StrictAttemptRecord>,
    var order: MutableList<String>,
) : StrictAttemptRecordStore {
    override fun find(code: String): StrictAttemptRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAttemptRecord) {
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

    override fun list(): MutableList<StrictAttemptRecord> {
        val out = mutableListOf<StrictAttemptRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAttemptRecordStore(): StrictAttemptRecordStore {
    return MemoryStrictAttemptRecordStore(mutableMapOf<String, StrictAttemptRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAttemptRecordStore(
    var inner: StrictAttemptRecordStore,
    var cache: TtlCache<StrictAttemptRecord>,
    var hits: Int,
    var misses: Int,
) : StrictAttemptRecordStore {
    override fun find(code: String): StrictAttemptRecord? {
        val hit = (cache.get(code) as StrictAttemptRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAttemptRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAttemptRecord> = inner.list()
}

fun newCachedStrictAttemptRecordStore(inner: StrictAttemptRecordStore, ttl: Int, clock: Clock): StrictAttemptRecordStore {
    return CachedStrictAttemptRecordStore(
        inner = inner,
        cache = newTtlCache<StrictAttemptRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict attempt records. Empty fields match all. */
data class StrictAttemptRecordQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAttemptRecord): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<StrictAttemptRecord>): MutableList<StrictAttemptRecord> {
        val matched = filter(items) { candidate: StrictAttemptRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAttemptRecords(store: StrictAttemptRecordStore, query: StrictAttemptRecordQuery): MutableList<StrictAttemptRecord> {
    return query.apply(store.list())
}
