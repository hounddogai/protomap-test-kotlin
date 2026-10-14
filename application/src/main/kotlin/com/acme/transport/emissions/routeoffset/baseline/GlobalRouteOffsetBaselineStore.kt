package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global route offset baselines by code. */
interface GlobalRouteOffsetBaselineStore {
    fun find(code: String): GlobalRouteOffsetBaseline?
    fun save(item: GlobalRouteOffsetBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRouteOffsetBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRouteOffsetBaselineStore(
    var items: MutableMap<String, GlobalRouteOffsetBaseline>,
    var order: MutableList<String>,
) : GlobalRouteOffsetBaselineStore {
    override fun find(code: String): GlobalRouteOffsetBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRouteOffsetBaseline) {
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

    override fun list(): MutableList<GlobalRouteOffsetBaseline> {
        val out = mutableListOf<GlobalRouteOffsetBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRouteOffsetBaselineStore(): GlobalRouteOffsetBaselineStore {
    return MemoryGlobalRouteOffsetBaselineStore(
        mutableMapOf<String, GlobalRouteOffsetBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRouteOffsetBaselineStore(
    var inner: GlobalRouteOffsetBaselineStore,
    var cache: TtlCache<GlobalRouteOffsetBaseline>,
    var hits: Int,
    var misses: Int,
) : GlobalRouteOffsetBaselineStore {
    override fun find(code: String): GlobalRouteOffsetBaseline? {
        val hit = (cache.get(code) as GlobalRouteOffsetBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRouteOffsetBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRouteOffsetBaseline> = inner.list()
}

fun newCachedGlobalRouteOffsetBaselineStore(inner: GlobalRouteOffsetBaselineStore, ttl: Int, clock: Clock): GlobalRouteOffsetBaselineStore {
    return CachedGlobalRouteOffsetBaselineStore(
        inner = inner,
        cache = newTtlCache<GlobalRouteOffsetBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global route offset baselines. Empty fields match all. */
data class GlobalRouteOffsetBaselineQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRouteOffsetBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalRouteOffsetBaseline>): MutableList<GlobalRouteOffsetBaseline> {
        val matched = filter(items) { candidate: GlobalRouteOffsetBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRouteOffsetBaselines(store: GlobalRouteOffsetBaselineStore, query: GlobalRouteOffsetBaselineQuery): MutableList<GlobalRouteOffsetBaseline> {
    return query.apply(store.list())
}
