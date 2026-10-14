package com.acme.transport.crewing.restbreak.baseline

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break baselines by code. */
interface RestBreakBaselineStore {
    fun find(code: String): RestBreakBaseline?
    fun save(item: RestBreakBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakBaselineStore(
    var items: MutableMap<String, RestBreakBaseline>,
    var order: MutableList<String>,
) : RestBreakBaselineStore {
    override fun find(code: String): RestBreakBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakBaseline) {
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

    override fun list(): MutableList<RestBreakBaseline> {
        val out = mutableListOf<RestBreakBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakBaselineStore(): RestBreakBaselineStore {
    return MemoryRestBreakBaselineStore(mutableMapOf<String, RestBreakBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakBaselineStore(
    var inner: RestBreakBaselineStore,
    var cache: TtlCache<RestBreakBaseline>,
    var hits: Int,
    var misses: Int,
) : RestBreakBaselineStore {
    override fun find(code: String): RestBreakBaseline? {
        val hit = (cache.get(code) as RestBreakBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakBaseline> = inner.list()
}

fun newCachedRestBreakBaselineStore(inner: RestBreakBaselineStore, ttl: Int, clock: Clock): RestBreakBaselineStore {
    return CachedRestBreakBaselineStore(
        inner = inner,
        cache = newTtlCache<RestBreakBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break baselines. Empty fields match all. */
data class RestBreakBaselineQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakBaseline): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakBaseline>): MutableList<RestBreakBaseline> {
        val matched = filter(items) { candidate: RestBreakBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakBaselines(store: RestBreakBaselineStore, query: RestBreakBaselineQuery): MutableList<RestBreakBaseline> {
    return query.apply(store.list())
}
