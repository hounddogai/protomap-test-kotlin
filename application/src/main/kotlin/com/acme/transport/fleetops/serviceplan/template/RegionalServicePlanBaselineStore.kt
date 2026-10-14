package com.acme.transport.fleetops.serviceplan.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional service plan baselines by code. */
interface RegionalServicePlanBaselineStore {
    fun find(code: String): RegionalServicePlanBaseline?
    fun save(item: RegionalServicePlanBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServicePlanBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServicePlanBaselineStore(
    var items: MutableMap<String, RegionalServicePlanBaseline>,
    var order: MutableList<String>,
) : RegionalServicePlanBaselineStore {
    override fun find(code: String): RegionalServicePlanBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServicePlanBaseline) {
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

    override fun list(): MutableList<RegionalServicePlanBaseline> {
        val out = mutableListOf<RegionalServicePlanBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServicePlanBaselineStore(): RegionalServicePlanBaselineStore {
    return MemoryRegionalServicePlanBaselineStore(
        mutableMapOf<String, RegionalServicePlanBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServicePlanBaselineStore(
    var inner: RegionalServicePlanBaselineStore,
    var cache: TtlCache<RegionalServicePlanBaseline>,
    var hits: Int,
    var misses: Int,
) : RegionalServicePlanBaselineStore {
    override fun find(code: String): RegionalServicePlanBaseline? {
        val hit = (cache.get(code) as RegionalServicePlanBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServicePlanBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServicePlanBaseline> = inner.list()
}

fun newCachedRegionalServicePlanBaselineStore(inner: RegionalServicePlanBaselineStore, ttl: Int, clock: Clock): RegionalServicePlanBaselineStore {
    return CachedRegionalServicePlanBaselineStore(
        inner = inner,
        cache = newTtlCache<RegionalServicePlanBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service plan baselines. Empty fields match all. */
data class RegionalServicePlanBaselineQuery(
    var unit: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServicePlanBaseline): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<RegionalServicePlanBaseline>): MutableList<RegionalServicePlanBaseline> {
        val matched = filter(items) { candidate: RegionalServicePlanBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServicePlanBaselines(store: RegionalServicePlanBaselineStore, query: RegionalServicePlanBaselineQuery): MutableList<RegionalServicePlanBaseline> {
    return query.apply(store.list())
}
