package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped attempt records by code. */
interface CappedAttemptRecordStore {
    fun find(code: String): CappedAttemptRecord?
    fun save(item: CappedAttemptRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedAttemptRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedAttemptRecordStore(
    var items: MutableMap<String, CappedAttemptRecord>,
    var order: MutableList<String>,
) : CappedAttemptRecordStore {
    override fun find(code: String): CappedAttemptRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedAttemptRecord) {
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

    override fun list(): MutableList<CappedAttemptRecord> {
        val out = mutableListOf<CappedAttemptRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedAttemptRecordStore(): CappedAttemptRecordStore {
    return MemoryCappedAttemptRecordStore(mutableMapOf<String, CappedAttemptRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedAttemptRecordStore(
    var inner: CappedAttemptRecordStore,
    var cache: TtlCache<CappedAttemptRecord>,
    var hits: Int,
    var misses: Int,
) : CappedAttemptRecordStore {
    override fun find(code: String): CappedAttemptRecord? {
        val hit = (cache.get(code) as CappedAttemptRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedAttemptRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedAttemptRecord> = inner.list()
}

fun newCachedCappedAttemptRecordStore(inner: CappedAttemptRecordStore, ttl: Int, clock: Clock): CappedAttemptRecordStore {
    return CachedCappedAttemptRecordStore(
        inner = inner,
        cache = newTtlCache<CappedAttemptRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped attempt records. Empty fields match all. */
data class CappedAttemptRecordQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedAttemptRecord): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedAttemptRecord>): MutableList<CappedAttemptRecord> {
        val matched = filter(items) { candidate: CappedAttemptRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedAttemptRecords(store: CappedAttemptRecordStore, query: CappedAttemptRecordQuery): MutableList<CappedAttemptRecord> {
    return query.apply(store.list())
}
