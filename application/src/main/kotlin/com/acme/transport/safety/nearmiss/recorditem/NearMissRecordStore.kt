package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss records by code. */
interface NearMissRecordStore {
    fun find(code: String): NearMissRecord?
    fun save(item: NearMissRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissRecordStore(
    var items: MutableMap<String, NearMissRecord>,
    var order: MutableList<String>,
) : NearMissRecordStore {
    override fun find(code: String): NearMissRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissRecord) {
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

    override fun list(): MutableList<NearMissRecord> {
        val out = mutableListOf<NearMissRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissRecordStore(): NearMissRecordStore {
    return MemoryNearMissRecordStore(mutableMapOf<String, NearMissRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissRecordStore(
    var inner: NearMissRecordStore,
    var cache: TtlCache<NearMissRecord>,
    var hits: Int,
    var misses: Int,
) : NearMissRecordStore {
    override fun find(code: String): NearMissRecord? {
        val hit = (cache.get(code) as NearMissRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissRecord> = inner.list()
}

fun newCachedNearMissRecordStore(inner: NearMissRecordStore, ttl: Int, clock: Clock): NearMissRecordStore {
    return CachedNearMissRecordStore(
        inner = inner,
        cache = newTtlCache<NearMissRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss records. Empty fields match all. */
data class NearMissRecordQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<NearMissRecord>): MutableList<NearMissRecord> {
        val matched = filter(items) { candidate: NearMissRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissRecords(store: NearMissRecordStore, query: NearMissRecordQuery): MutableList<NearMissRecord> {
    return query.apply(store.list())
}
