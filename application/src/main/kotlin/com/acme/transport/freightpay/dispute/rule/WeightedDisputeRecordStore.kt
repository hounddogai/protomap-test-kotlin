package com.acme.transport.freightpay.dispute.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted dispute records by code. */
interface WeightedDisputeRecordStore {
    fun find(code: String): WeightedDisputeRecord?
    fun save(item: WeightedDisputeRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDisputeRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDisputeRecordStore(
    var items: MutableMap<String, WeightedDisputeRecord>,
    var order: MutableList<String>,
) : WeightedDisputeRecordStore {
    override fun find(code: String): WeightedDisputeRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDisputeRecord) {
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

    override fun list(): MutableList<WeightedDisputeRecord> {
        val out = mutableListOf<WeightedDisputeRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDisputeRecordStore(): WeightedDisputeRecordStore {
    return MemoryWeightedDisputeRecordStore(
        mutableMapOf<String, WeightedDisputeRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDisputeRecordStore(
    var inner: WeightedDisputeRecordStore,
    var cache: TtlCache<WeightedDisputeRecord>,
    var hits: Int,
    var misses: Int,
) : WeightedDisputeRecordStore {
    override fun find(code: String): WeightedDisputeRecord? {
        val hit = (cache.get(code) as WeightedDisputeRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDisputeRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDisputeRecord> = inner.list()
}

fun newCachedWeightedDisputeRecordStore(inner: WeightedDisputeRecordStore, ttl: Int, clock: Clock): WeightedDisputeRecordStore {
    return CachedWeightedDisputeRecordStore(
        inner = inner,
        cache = newTtlCache<WeightedDisputeRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dispute records. Empty fields match all. */
data class WeightedDisputeRecordQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDisputeRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDisputeRecord>): MutableList<WeightedDisputeRecord> {
        val matched = filter(items) { candidate: WeightedDisputeRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDisputeRecords(store: WeightedDisputeRecordStore, query: WeightedDisputeRecordQuery): MutableList<WeightedDisputeRecord> {
    return query.apply(store.list())
}
