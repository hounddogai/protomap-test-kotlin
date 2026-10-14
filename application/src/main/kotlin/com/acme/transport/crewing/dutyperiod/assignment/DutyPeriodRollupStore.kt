package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period rollups by code. */
interface DutyPeriodRollupStore {
    fun find(code: String): DutyPeriodRollup?
    fun save(item: DutyPeriodRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodRollupStore(
    var items: MutableMap<String, DutyPeriodRollup>,
    var order: MutableList<String>,
) : DutyPeriodRollupStore {
    override fun find(code: String): DutyPeriodRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodRollup) {
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

    override fun list(): MutableList<DutyPeriodRollup> {
        val out = mutableListOf<DutyPeriodRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodRollupStore(): DutyPeriodRollupStore {
    return MemoryDutyPeriodRollupStore(mutableMapOf<String, DutyPeriodRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodRollupStore(
    var inner: DutyPeriodRollupStore,
    var cache: TtlCache<DutyPeriodRollup>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodRollupStore {
    override fun find(code: String): DutyPeriodRollup? {
        val hit = (cache.get(code) as DutyPeriodRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodRollup> = inner.list()
}

fun newCachedDutyPeriodRollupStore(inner: DutyPeriodRollupStore, ttl: Int, clock: Clock): DutyPeriodRollupStore {
    return CachedDutyPeriodRollupStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period rollups. Empty fields match all. */
data class DutyPeriodRollupQuery(
    var depot: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodRollup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodRollup>): MutableList<DutyPeriodRollup> {
        val matched = filter(items) { candidate: DutyPeriodRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodRollups(store: DutyPeriodRollupStore, query: DutyPeriodRollupQuery): MutableList<DutyPeriodRollup> {
    return query.apply(store.list())
}
