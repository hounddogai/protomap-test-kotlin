package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual route offset baselines by code. */
interface ManualRouteOffsetBaselineStore {
    fun find(code: String): ManualRouteOffsetBaseline?
    fun save(item: ManualRouteOffsetBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRouteOffsetBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRouteOffsetBaselineStore(
    var items: MutableMap<String, ManualRouteOffsetBaseline>,
    var order: MutableList<String>,
) : ManualRouteOffsetBaselineStore {
    override fun find(code: String): ManualRouteOffsetBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRouteOffsetBaseline) {
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

    override fun list(): MutableList<ManualRouteOffsetBaseline> {
        val out = mutableListOf<ManualRouteOffsetBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRouteOffsetBaselineStore(): ManualRouteOffsetBaselineStore {
    return MemoryManualRouteOffsetBaselineStore(
        mutableMapOf<String, ManualRouteOffsetBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRouteOffsetBaselineStore(
    var inner: ManualRouteOffsetBaselineStore,
    var cache: TtlCache<ManualRouteOffsetBaseline>,
    var hits: Int,
    var misses: Int,
) : ManualRouteOffsetBaselineStore {
    override fun find(code: String): ManualRouteOffsetBaseline? {
        val hit = (cache.get(code) as ManualRouteOffsetBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRouteOffsetBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRouteOffsetBaseline> = inner.list()
}

fun newCachedManualRouteOffsetBaselineStore(inner: ManualRouteOffsetBaselineStore, ttl: Int, clock: Clock): ManualRouteOffsetBaselineStore {
    return CachedManualRouteOffsetBaselineStore(
        inner = inner,
        cache = newTtlCache<ManualRouteOffsetBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual route offset baselines. Empty fields match all. */
data class ManualRouteOffsetBaselineQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRouteOffsetBaseline): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualRouteOffsetBaseline>): MutableList<ManualRouteOffsetBaseline> {
        val matched = filter(items) { candidate: ManualRouteOffsetBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRouteOffsetBaselines(store: ManualRouteOffsetBaselineStore, query: ManualRouteOffsetBaselineQuery): MutableList<ManualRouteOffsetBaseline> {
    return query.apply(store.list())
}
