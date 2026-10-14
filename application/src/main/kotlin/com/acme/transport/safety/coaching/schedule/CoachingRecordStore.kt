package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching records by code. */
interface CoachingRecordStore {
    fun find(code: String): CoachingRecord?
    fun save(item: CoachingRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingRecordStore(
    var items: MutableMap<String, CoachingRecord>,
    var order: MutableList<String>,
) : CoachingRecordStore {
    override fun find(code: String): CoachingRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingRecord) {
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

    override fun list(): MutableList<CoachingRecord> {
        val out = mutableListOf<CoachingRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingRecordStore(): CoachingRecordStore {
    return MemoryCoachingRecordStore(mutableMapOf<String, CoachingRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingRecordStore(
    var inner: CoachingRecordStore,
    var cache: TtlCache<CoachingRecord>,
    var hits: Int,
    var misses: Int,
) : CoachingRecordStore {
    override fun find(code: String): CoachingRecord? {
        val hit = (cache.get(code) as CoachingRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingRecord> = inner.list()
}

fun newCachedCoachingRecordStore(inner: CoachingRecordStore, ttl: Int, clock: Clock): CoachingRecordStore {
    return CachedCoachingRecordStore(
        inner = inner,
        cache = newTtlCache<CoachingRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching records. Empty fields match all. */
data class CoachingRecordQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingRecord>): MutableList<CoachingRecord> {
        val matched = filter(items) { candidate: CoachingRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingRecords(store: CoachingRecordStore, query: CoachingRecordQuery): MutableList<CoachingRecord> {
    return query.apply(store.list())
}
