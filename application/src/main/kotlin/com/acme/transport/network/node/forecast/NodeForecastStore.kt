package com.acme.transport.network.node.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps node forecasts by code. */
interface NodeForecastStore {
    fun find(code: String): NodeForecast?
    fun save(item: NodeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<NodeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNodeForecastStore(
    var items: MutableMap<String, NodeForecast>,
    var order: MutableList<String>,
) : NodeForecastStore {
    override fun find(code: String): NodeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NodeForecast) {
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

    override fun list(): MutableList<NodeForecast> {
        val out = mutableListOf<NodeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNodeForecastStore(): NodeForecastStore {
    return MemoryNodeForecastStore(mutableMapOf<String, NodeForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNodeForecastStore(
    var inner: NodeForecastStore,
    var cache: TtlCache<NodeForecast>,
    var hits: Int,
    var misses: Int,
) : NodeForecastStore {
    override fun find(code: String): NodeForecast? {
        val hit = (cache.get(code) as NodeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NodeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NodeForecast> = inner.list()
}

fun newCachedNodeForecastStore(inner: NodeForecastStore, ttl: Int, clock: Clock): NodeForecastStore {
    return CachedNodeForecastStore(
        inner = inner,
        cache = newTtlCache<NodeForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects node forecasts. Empty fields match all. */
data class NodeForecastQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NodeForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NodeForecast>): MutableList<NodeForecast> {
        val matched = filter(items) { candidate: NodeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNodeForecasts(store: NodeForecastStore, query: NodeForecastQuery): MutableList<NodeForecast> {
    return query.apply(store.list())
}
