package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped trailer records by code. */
interface CappedTrailerRecordStore {
    fun find(code: String): CappedTrailerRecord?
    fun save(item: CappedTrailerRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTrailerRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTrailerRecordStore(
    var items: MutableMap<String, CappedTrailerRecord>,
    var order: MutableList<String>,
) : CappedTrailerRecordStore {
    override fun find(code: String): CappedTrailerRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTrailerRecord) {
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

    override fun list(): MutableList<CappedTrailerRecord> {
        val out = mutableListOf<CappedTrailerRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTrailerRecordStore(): CappedTrailerRecordStore {
    return MemoryCappedTrailerRecordStore(mutableMapOf<String, CappedTrailerRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTrailerRecordStore(
    var inner: CappedTrailerRecordStore,
    var cache: TtlCache<CappedTrailerRecord>,
    var hits: Int,
    var misses: Int,
) : CappedTrailerRecordStore {
    override fun find(code: String): CappedTrailerRecord? {
        val hit = (cache.get(code) as CappedTrailerRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTrailerRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTrailerRecord> = inner.list()
}

fun newCachedCappedTrailerRecordStore(inner: CappedTrailerRecordStore, ttl: Int, clock: Clock): CappedTrailerRecordStore {
    return CachedCappedTrailerRecordStore(
        inner = inner,
        cache = newTtlCache<CappedTrailerRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped trailer records. Empty fields match all. */
data class CappedTrailerRecordQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTrailerRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedTrailerRecord>): MutableList<CappedTrailerRecord> {
        val matched = filter(items) { candidate: CappedTrailerRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTrailerRecords(store: CappedTrailerRecordStore, query: CappedTrailerRecordQuery): MutableList<CappedTrailerRecord> {
    return query.apply(store.list())
}
