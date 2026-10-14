package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback service plan rollups by code. */
interface FallbackServicePlanRollupStore {
    fun find(code: String): FallbackServicePlanRollup?
    fun save(item: FallbackServicePlanRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackServicePlanRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackServicePlanRollupStore(
    var items: MutableMap<String, FallbackServicePlanRollup>,
    var order: MutableList<String>,
) : FallbackServicePlanRollupStore {
    override fun find(code: String): FallbackServicePlanRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackServicePlanRollup) {
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

    override fun list(): MutableList<FallbackServicePlanRollup> {
        val out = mutableListOf<FallbackServicePlanRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackServicePlanRollupStore(): FallbackServicePlanRollupStore {
    return MemoryFallbackServicePlanRollupStore(
        mutableMapOf<String, FallbackServicePlanRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackServicePlanRollupStore(
    var inner: FallbackServicePlanRollupStore,
    var cache: TtlCache<FallbackServicePlanRollup>,
    var hits: Int,
    var misses: Int,
) : FallbackServicePlanRollupStore {
    override fun find(code: String): FallbackServicePlanRollup? {
        val hit = (cache.get(code) as FallbackServicePlanRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackServicePlanRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackServicePlanRollup> = inner.list()
}

fun newCachedFallbackServicePlanRollupStore(inner: FallbackServicePlanRollupStore, ttl: Int, clock: Clock): FallbackServicePlanRollupStore {
    return CachedFallbackServicePlanRollupStore(
        inner = inner,
        cache = newTtlCache<FallbackServicePlanRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback service plan rollups. Empty fields match all. */
data class FallbackServicePlanRollupQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackServicePlanRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackServicePlanRollup>): MutableList<FallbackServicePlanRollup> {
        val matched = filter(items) { candidate: FallbackServicePlanRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackServicePlanRollups(store: FallbackServicePlanRollupStore, query: FallbackServicePlanRollupQuery): MutableList<FallbackServicePlanRollup> {
    return query.apply(store.list())
}
