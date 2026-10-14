package com.acme.transport.crewing.swap.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted swap records by code. */
interface WeightedSwapRecordStore {
    fun find(code: String): WeightedSwapRecord?
    fun save(item: WeightedSwapRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedSwapRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedSwapRecordStore(
    var items: MutableMap<String, WeightedSwapRecord>,
    var order: MutableList<String>,
) : WeightedSwapRecordStore {
    override fun find(code: String): WeightedSwapRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedSwapRecord) {
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

    override fun list(): MutableList<WeightedSwapRecord> {
        val out = mutableListOf<WeightedSwapRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedSwapRecordStore(): WeightedSwapRecordStore {
    return MemoryWeightedSwapRecordStore(mutableMapOf<String, WeightedSwapRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedSwapRecordStore(
    var inner: WeightedSwapRecordStore,
    var cache: TtlCache<WeightedSwapRecord>,
    var hits: Int,
    var misses: Int,
) : WeightedSwapRecordStore {
    override fun find(code: String): WeightedSwapRecord? {
        val hit = (cache.get(code) as WeightedSwapRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedSwapRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedSwapRecord> = inner.list()
}

fun newCachedWeightedSwapRecordStore(inner: WeightedSwapRecordStore, ttl: Int, clock: Clock): WeightedSwapRecordStore {
    return CachedWeightedSwapRecordStore(
        inner = inner,
        cache = newTtlCache<WeightedSwapRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted swap records. Empty fields match all. */
data class WeightedSwapRecordQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedSwapRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedSwapRecord>): MutableList<WeightedSwapRecord> {
        val matched = filter(items) { candidate: WeightedSwapRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedSwapRecords(store: WeightedSwapRecordStore, query: WeightedSwapRecordQuery): MutableList<WeightedSwapRecord> {
    return query.apply(store.list())
}
