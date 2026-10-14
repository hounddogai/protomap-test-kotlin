package com.acme.transport.crewing.swap.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap records by code. */
interface SwapRecordStore {
    fun find(code: String): SwapRecord?
    fun save(item: SwapRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapRecordStore(
    var items: MutableMap<String, SwapRecord>,
    var order: MutableList<String>,
) : SwapRecordStore {
    override fun find(code: String): SwapRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapRecord) {
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

    override fun list(): MutableList<SwapRecord> {
        val out = mutableListOf<SwapRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapRecordStore(): SwapRecordStore {
    return MemorySwapRecordStore(mutableMapOf<String, SwapRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapRecordStore(
    var inner: SwapRecordStore,
    var cache: TtlCache<SwapRecord>,
    var hits: Int,
    var misses: Int,
) : SwapRecordStore {
    override fun find(code: String): SwapRecord? {
        val hit = (cache.get(code) as SwapRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapRecord> = inner.list()
}

fun newCachedSwapRecordStore(inner: SwapRecordStore, ttl: Int, clock: Clock): SwapRecordStore {
    return CachedSwapRecordStore(
        inner = inner,
        cache = newTtlCache<SwapRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap records. Empty fields match all. */
data class SwapRecordQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapRecord): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<SwapRecord>): MutableList<SwapRecord> {
        val matched = filter(items) { candidate: SwapRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapRecords(store: SwapRecordStore, query: SwapRecordQuery): MutableList<SwapRecord> {
    return query.apply(store.list())
}
