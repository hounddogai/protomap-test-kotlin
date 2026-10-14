package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps pending locker records by code. */
interface PendingLockerRecordStore {
    fun find(code: String): PendingLockerRecord?
    fun save(item: PendingLockerRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLockerRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLockerRecordStore(
    var items: MutableMap<String, PendingLockerRecord>,
    var order: MutableList<String>,
) : PendingLockerRecordStore {
    override fun find(code: String): PendingLockerRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLockerRecord) {
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

    override fun list(): MutableList<PendingLockerRecord> {
        val out = mutableListOf<PendingLockerRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLockerRecordStore(): PendingLockerRecordStore {
    return MemoryPendingLockerRecordStore(mutableMapOf<String, PendingLockerRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLockerRecordStore(
    var inner: PendingLockerRecordStore,
    var cache: TtlCache<PendingLockerRecord>,
    var hits: Int,
    var misses: Int,
) : PendingLockerRecordStore {
    override fun find(code: String): PendingLockerRecord? {
        val hit = (cache.get(code) as PendingLockerRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLockerRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLockerRecord> = inner.list()
}

fun newCachedPendingLockerRecordStore(inner: PendingLockerRecordStore, ttl: Int, clock: Clock): PendingLockerRecordStore {
    return CachedPendingLockerRecordStore(
        inner = inner,
        cache = newTtlCache<PendingLockerRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending locker records. Empty fields match all. */
data class PendingLockerRecordQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLockerRecord): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLockerRecord>): MutableList<PendingLockerRecord> {
        val matched = filter(items) { candidate: PendingLockerRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLockerRecords(store: PendingLockerRecordStore, query: PendingLockerRecordQuery): MutableList<PendingLockerRecord> {
    return query.apply(store.list())
}
