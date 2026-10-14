package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps scheduled near miss records by code. */
interface ScheduledNearMissRecordStore {
    fun find(code: String): ScheduledNearMissRecord?
    fun save(item: ScheduledNearMissRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNearMissRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNearMissRecordStore(
    var items: MutableMap<String, ScheduledNearMissRecord>,
    var order: MutableList<String>,
) : ScheduledNearMissRecordStore {
    override fun find(code: String): ScheduledNearMissRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNearMissRecord) {
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

    override fun list(): MutableList<ScheduledNearMissRecord> {
        val out = mutableListOf<ScheduledNearMissRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNearMissRecordStore(): ScheduledNearMissRecordStore {
    return MemoryScheduledNearMissRecordStore(
        mutableMapOf<String, ScheduledNearMissRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNearMissRecordStore(
    var inner: ScheduledNearMissRecordStore,
    var cache: TtlCache<ScheduledNearMissRecord>,
    var hits: Int,
    var misses: Int,
) : ScheduledNearMissRecordStore {
    override fun find(code: String): ScheduledNearMissRecord? {
        val hit = (cache.get(code) as ScheduledNearMissRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNearMissRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNearMissRecord> = inner.list()
}

fun newCachedScheduledNearMissRecordStore(inner: ScheduledNearMissRecordStore, ttl: Int, clock: Clock): ScheduledNearMissRecordStore {
    return CachedScheduledNearMissRecordStore(
        inner = inner,
        cache = newTtlCache<ScheduledNearMissRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled near miss records. Empty fields match all. */
data class ScheduledNearMissRecordQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNearMissRecord): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledNearMissRecord>): MutableList<ScheduledNearMissRecord> {
        val matched = filter(items) { candidate: ScheduledNearMissRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNearMissRecords(store: ScheduledNearMissRecordStore, query: ScheduledNearMissRecordQuery): MutableList<ScheduledNearMissRecord> {
    return query.apply(store.list())
}
