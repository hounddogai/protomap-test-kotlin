package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break rollups by code. */
interface RestBreakRollupStore {
    fun find(code: String): RestBreakRollup?
    fun save(item: RestBreakRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakRollupStore(
    var items: MutableMap<String, RestBreakRollup>,
    var order: MutableList<String>,
) : RestBreakRollupStore {
    override fun find(code: String): RestBreakRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakRollup) {
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

    override fun list(): MutableList<RestBreakRollup> {
        val out = mutableListOf<RestBreakRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakRollupStore(): RestBreakRollupStore {
    return MemoryRestBreakRollupStore(mutableMapOf<String, RestBreakRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakRollupStore(
    var inner: RestBreakRollupStore,
    var cache: TtlCache<RestBreakRollup>,
    var hits: Int,
    var misses: Int,
) : RestBreakRollupStore {
    override fun find(code: String): RestBreakRollup? {
        val hit = (cache.get(code) as RestBreakRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakRollup> = inner.list()
}

fun newCachedRestBreakRollupStore(inner: RestBreakRollupStore, ttl: Int, clock: Clock): RestBreakRollupStore {
    return CachedRestBreakRollupStore(
        inner = inner,
        cache = newTtlCache<RestBreakRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break rollups. Empty fields match all. */
data class RestBreakRollupQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakRollup>): MutableList<RestBreakRollup> {
        val matched = filter(items) { candidate: RestBreakRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakRollups(store: RestBreakRollupStore, query: RestBreakRollupQuery): MutableList<RestBreakRollup> {
    return query.apply(store.list())
}
