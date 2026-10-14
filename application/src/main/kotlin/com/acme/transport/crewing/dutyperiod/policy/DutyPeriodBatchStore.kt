package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period batches by code. */
interface DutyPeriodBatchStore {
    fun find(code: String): DutyPeriodBatch?
    fun save(item: DutyPeriodBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodBatchStore(
    var items: MutableMap<String, DutyPeriodBatch>,
    var order: MutableList<String>,
) : DutyPeriodBatchStore {
    override fun find(code: String): DutyPeriodBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodBatch) {
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

    override fun list(): MutableList<DutyPeriodBatch> {
        val out = mutableListOf<DutyPeriodBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodBatchStore(): DutyPeriodBatchStore {
    return MemoryDutyPeriodBatchStore(mutableMapOf<String, DutyPeriodBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodBatchStore(
    var inner: DutyPeriodBatchStore,
    var cache: TtlCache<DutyPeriodBatch>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodBatchStore {
    override fun find(code: String): DutyPeriodBatch? {
        val hit = (cache.get(code) as DutyPeriodBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodBatch> = inner.list()
}

fun newCachedDutyPeriodBatchStore(inner: DutyPeriodBatchStore, ttl: Int, clock: Clock): DutyPeriodBatchStore {
    return CachedDutyPeriodBatchStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period batches. Empty fields match all. */
data class DutyPeriodBatchQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodBatch): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodBatch>): MutableList<DutyPeriodBatch> {
        val matched = filter(items) { candidate: DutyPeriodBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodBatches(store: DutyPeriodBatchStore, query: DutyPeriodBatchQuery): MutableList<DutyPeriodBatch> {
    return query.apply(store.list())
}
