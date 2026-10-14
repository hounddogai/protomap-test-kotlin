package com.acme.transport.crewing.dutyperiod.window

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal duty period batches by code. */
interface SeasonalDutyPeriodBatchStore {
    fun find(code: String): SeasonalDutyPeriodBatch?
    fun save(item: SeasonalDutyPeriodBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDutyPeriodBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDutyPeriodBatchStore(
    var items: MutableMap<String, SeasonalDutyPeriodBatch>,
    var order: MutableList<String>,
) : SeasonalDutyPeriodBatchStore {
    override fun find(code: String): SeasonalDutyPeriodBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDutyPeriodBatch) {
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

    override fun list(): MutableList<SeasonalDutyPeriodBatch> {
        val out = mutableListOf<SeasonalDutyPeriodBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDutyPeriodBatchStore(): SeasonalDutyPeriodBatchStore {
    return MemorySeasonalDutyPeriodBatchStore(
        mutableMapOf<String, SeasonalDutyPeriodBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDutyPeriodBatchStore(
    var inner: SeasonalDutyPeriodBatchStore,
    var cache: TtlCache<SeasonalDutyPeriodBatch>,
    var hits: Int,
    var misses: Int,
) : SeasonalDutyPeriodBatchStore {
    override fun find(code: String): SeasonalDutyPeriodBatch? {
        val hit = (cache.get(code) as SeasonalDutyPeriodBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDutyPeriodBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDutyPeriodBatch> = inner.list()
}

fun newCachedSeasonalDutyPeriodBatchStore(inner: SeasonalDutyPeriodBatchStore, ttl: Int, clock: Clock): SeasonalDutyPeriodBatchStore {
    return CachedSeasonalDutyPeriodBatchStore(
        inner = inner,
        cache = newTtlCache<SeasonalDutyPeriodBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal duty period batches. Empty fields match all. */
data class SeasonalDutyPeriodBatchQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDutyPeriodBatch): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDutyPeriodBatch>): MutableList<SeasonalDutyPeriodBatch> {
        val matched = filter(items) { candidate: SeasonalDutyPeriodBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDutyPeriodBatches(store: SeasonalDutyPeriodBatchStore, query: SeasonalDutyPeriodBatchQuery): MutableList<SeasonalDutyPeriodBatch> {
    return query.apply(store.list())
}
