package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped near miss records by code. */
interface CappedNearMissRecordStore {
    fun find(code: String): CappedNearMissRecord?
    fun save(item: CappedNearMissRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedNearMissRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedNearMissRecordStore(
    var items: MutableMap<String, CappedNearMissRecord>,
    var order: MutableList<String>,
) : CappedNearMissRecordStore {
    override fun find(code: String): CappedNearMissRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedNearMissRecord) {
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

    override fun list(): MutableList<CappedNearMissRecord> {
        val out = mutableListOf<CappedNearMissRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedNearMissRecordStore(): CappedNearMissRecordStore {
    return MemoryCappedNearMissRecordStore(
        mutableMapOf<String, CappedNearMissRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedNearMissRecordStore(
    var inner: CappedNearMissRecordStore,
    var cache: TtlCache<CappedNearMissRecord>,
    var hits: Int,
    var misses: Int,
) : CappedNearMissRecordStore {
    override fun find(code: String): CappedNearMissRecord? {
        val hit = (cache.get(code) as CappedNearMissRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedNearMissRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedNearMissRecord> = inner.list()
}

fun newCachedCappedNearMissRecordStore(inner: CappedNearMissRecordStore, ttl: Int, clock: Clock): CappedNearMissRecordStore {
    return CachedCappedNearMissRecordStore(
        inner = inner,
        cache = newTtlCache<CappedNearMissRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped near miss records. Empty fields match all. */
data class CappedNearMissRecordQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedNearMissRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<CappedNearMissRecord>): MutableList<CappedNearMissRecord> {
        val matched = filter(items) { candidate: CappedNearMissRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedNearMissRecords(store: CappedNearMissRecordStore, query: CappedNearMissRecordQuery): MutableList<CappedNearMissRecord> {
    return query.apply(store.list())
}
