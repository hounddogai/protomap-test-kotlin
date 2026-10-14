package com.acme.transport.crewing.swap.swaprecord

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped swap records by code. */
interface CappedSwapRecordStore {
    fun find(code: String): CappedSwapRecord?
    fun save(item: CappedSwapRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedSwapRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedSwapRecordStore(
    var items: MutableMap<String, CappedSwapRecord>,
    var order: MutableList<String>,
) : CappedSwapRecordStore {
    override fun find(code: String): CappedSwapRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedSwapRecord) {
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

    override fun list(): MutableList<CappedSwapRecord> {
        val out = mutableListOf<CappedSwapRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedSwapRecordStore(): CappedSwapRecordStore {
    return MemoryCappedSwapRecordStore(mutableMapOf<String, CappedSwapRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedSwapRecordStore(
    var inner: CappedSwapRecordStore,
    var cache: TtlCache<CappedSwapRecord>,
    var hits: Int,
    var misses: Int,
) : CappedSwapRecordStore {
    override fun find(code: String): CappedSwapRecord? {
        val hit = (cache.get(code) as CappedSwapRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedSwapRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedSwapRecord> = inner.list()
}

fun newCachedCappedSwapRecordStore(inner: CappedSwapRecordStore, ttl: Int, clock: Clock): CappedSwapRecordStore {
    return CachedCappedSwapRecordStore(
        inner = inner,
        cache = newTtlCache<CappedSwapRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped swap records. Empty fields match all. */
data class CappedSwapRecordQuery(
    var country: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedSwapRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedSwapRecord>): MutableList<CappedSwapRecord> {
        val matched = filter(items) { candidate: CappedSwapRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedSwapRecords(store: CappedSwapRecordStore, query: CappedSwapRecordQuery): MutableList<CappedSwapRecord> {
    return query.apply(store.list())
}
