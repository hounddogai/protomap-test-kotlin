package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy service plan baselines by code. */
interface LegacyServicePlanBaselineStore {
    fun find(code: String): LegacyServicePlanBaseline?
    fun save(item: LegacyServicePlanBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyServicePlanBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyServicePlanBaselineStore(
    var items: MutableMap<String, LegacyServicePlanBaseline>,
    var order: MutableList<String>,
) : LegacyServicePlanBaselineStore {
    override fun find(code: String): LegacyServicePlanBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyServicePlanBaseline) {
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

    override fun list(): MutableList<LegacyServicePlanBaseline> {
        val out = mutableListOf<LegacyServicePlanBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyServicePlanBaselineStore(): LegacyServicePlanBaselineStore {
    return MemoryLegacyServicePlanBaselineStore(
        mutableMapOf<String, LegacyServicePlanBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyServicePlanBaselineStore(
    var inner: LegacyServicePlanBaselineStore,
    var cache: TtlCache<LegacyServicePlanBaseline>,
    var hits: Int,
    var misses: Int,
) : LegacyServicePlanBaselineStore {
    override fun find(code: String): LegacyServicePlanBaseline? {
        val hit = (cache.get(code) as LegacyServicePlanBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyServicePlanBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyServicePlanBaseline> = inner.list()
}

fun newCachedLegacyServicePlanBaselineStore(inner: LegacyServicePlanBaselineStore, ttl: Int, clock: Clock): LegacyServicePlanBaselineStore {
    return CachedLegacyServicePlanBaselineStore(
        inner = inner,
        cache = newTtlCache<LegacyServicePlanBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy service plan baselines. Empty fields match all. */
data class LegacyServicePlanBaselineQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyServicePlanBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyServicePlanBaseline>): MutableList<LegacyServicePlanBaseline> {
        val matched = filter(items) { candidate: LegacyServicePlanBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyServicePlanBaselines(store: LegacyServicePlanBaselineStore, query: LegacyServicePlanBaselineQuery): MutableList<LegacyServicePlanBaseline> {
    return query.apply(store.list())
}
