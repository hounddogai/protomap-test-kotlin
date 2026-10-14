package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period baselines by code. */
interface DutyPeriodBaselineStore {
    fun find(code: String): DutyPeriodBaseline?
    fun save(item: DutyPeriodBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodBaselineStore(
    var items: MutableMap<String, DutyPeriodBaseline>,
    var order: MutableList<String>,
) : DutyPeriodBaselineStore {
    override fun find(code: String): DutyPeriodBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodBaseline) {
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

    override fun list(): MutableList<DutyPeriodBaseline> {
        val out = mutableListOf<DutyPeriodBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodBaselineStore(): DutyPeriodBaselineStore {
    return MemoryDutyPeriodBaselineStore(mutableMapOf<String, DutyPeriodBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodBaselineStore(
    var inner: DutyPeriodBaselineStore,
    var cache: TtlCache<DutyPeriodBaseline>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodBaselineStore {
    override fun find(code: String): DutyPeriodBaseline? {
        val hit = (cache.get(code) as DutyPeriodBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodBaseline> = inner.list()
}

fun newCachedDutyPeriodBaselineStore(inner: DutyPeriodBaselineStore, ttl: Int, clock: Clock): DutyPeriodBaselineStore {
    return CachedDutyPeriodBaselineStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period baselines. Empty fields match all. */
data class DutyPeriodBaselineQuery(
    var segment: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodBaseline): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodBaseline>): MutableList<DutyPeriodBaseline> {
        val matched = filter(items) { candidate: DutyPeriodBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodBaselines(store: DutyPeriodBaselineStore, query: DutyPeriodBaselineQuery): MutableList<DutyPeriodBaseline> {
    return query.apply(store.list())
}
