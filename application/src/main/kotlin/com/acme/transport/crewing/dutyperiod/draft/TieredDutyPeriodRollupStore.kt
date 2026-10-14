package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered duty period rollups by code. */
interface TieredDutyPeriodRollupStore {
    fun find(code: String): TieredDutyPeriodRollup?
    fun save(item: TieredDutyPeriodRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDutyPeriodRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDutyPeriodRollupStore(
    var items: MutableMap<String, TieredDutyPeriodRollup>,
    var order: MutableList<String>,
) : TieredDutyPeriodRollupStore {
    override fun find(code: String): TieredDutyPeriodRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDutyPeriodRollup) {
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

    override fun list(): MutableList<TieredDutyPeriodRollup> {
        val out = mutableListOf<TieredDutyPeriodRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDutyPeriodRollupStore(): TieredDutyPeriodRollupStore {
    return MemoryTieredDutyPeriodRollupStore(
        mutableMapOf<String, TieredDutyPeriodRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDutyPeriodRollupStore(
    var inner: TieredDutyPeriodRollupStore,
    var cache: TtlCache<TieredDutyPeriodRollup>,
    var hits: Int,
    var misses: Int,
) : TieredDutyPeriodRollupStore {
    override fun find(code: String): TieredDutyPeriodRollup? {
        val hit = (cache.get(code) as TieredDutyPeriodRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDutyPeriodRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDutyPeriodRollup> = inner.list()
}

fun newCachedTieredDutyPeriodRollupStore(inner: TieredDutyPeriodRollupStore, ttl: Int, clock: Clock): TieredDutyPeriodRollupStore {
    return CachedTieredDutyPeriodRollupStore(
        inner = inner,
        cache = newTtlCache<TieredDutyPeriodRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered duty period rollups. Empty fields match all. */
data class TieredDutyPeriodRollupQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDutyPeriodRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDutyPeriodRollup>): MutableList<TieredDutyPeriodRollup> {
        val matched = filter(items) { candidate: TieredDutyPeriodRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDutyPeriodRollups(store: TieredDutyPeriodRollupStore, query: TieredDutyPeriodRollupQuery): MutableList<TieredDutyPeriodRollup> {
    return query.apply(store.list())
}
