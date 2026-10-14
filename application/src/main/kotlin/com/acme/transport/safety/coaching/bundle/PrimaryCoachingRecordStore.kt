package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary coaching records by code. */
interface PrimaryCoachingRecordStore {
    fun find(code: String): PrimaryCoachingRecord?
    fun save(item: PrimaryCoachingRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCoachingRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCoachingRecordStore(
    var items: MutableMap<String, PrimaryCoachingRecord>,
    var order: MutableList<String>,
) : PrimaryCoachingRecordStore {
    override fun find(code: String): PrimaryCoachingRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCoachingRecord) {
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

    override fun list(): MutableList<PrimaryCoachingRecord> {
        val out = mutableListOf<PrimaryCoachingRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCoachingRecordStore(): PrimaryCoachingRecordStore {
    return MemoryPrimaryCoachingRecordStore(
        mutableMapOf<String, PrimaryCoachingRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCoachingRecordStore(
    var inner: PrimaryCoachingRecordStore,
    var cache: TtlCache<PrimaryCoachingRecord>,
    var hits: Int,
    var misses: Int,
) : PrimaryCoachingRecordStore {
    override fun find(code: String): PrimaryCoachingRecord? {
        val hit = (cache.get(code) as PrimaryCoachingRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCoachingRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCoachingRecord> = inner.list()
}

fun newCachedPrimaryCoachingRecordStore(inner: PrimaryCoachingRecordStore, ttl: Int, clock: Clock): PrimaryCoachingRecordStore {
    return CachedPrimaryCoachingRecordStore(
        inner = inner,
        cache = newTtlCache<PrimaryCoachingRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary coaching records. Empty fields match all. */
data class PrimaryCoachingRecordQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCoachingRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCoachingRecord>): MutableList<PrimaryCoachingRecord> {
        val matched = filter(items) { candidate: PrimaryCoachingRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCoachingRecords(store: PrimaryCoachingRecordStore, query: PrimaryCoachingRecordQuery): MutableList<PrimaryCoachingRecord> {
    return query.apply(store.list())
}
