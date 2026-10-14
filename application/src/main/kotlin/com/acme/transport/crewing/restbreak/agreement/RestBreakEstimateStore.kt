package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break estimates by code. */
interface RestBreakEstimateStore {
    fun find(code: String): RestBreakEstimate?
    fun save(item: RestBreakEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakEstimateStore(
    var items: MutableMap<String, RestBreakEstimate>,
    var order: MutableList<String>,
) : RestBreakEstimateStore {
    override fun find(code: String): RestBreakEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakEstimate) {
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

    override fun list(): MutableList<RestBreakEstimate> {
        val out = mutableListOf<RestBreakEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakEstimateStore(): RestBreakEstimateStore {
    return MemoryRestBreakEstimateStore(mutableMapOf<String, RestBreakEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakEstimateStore(
    var inner: RestBreakEstimateStore,
    var cache: TtlCache<RestBreakEstimate>,
    var hits: Int,
    var misses: Int,
) : RestBreakEstimateStore {
    override fun find(code: String): RestBreakEstimate? {
        val hit = (cache.get(code) as RestBreakEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakEstimate> = inner.list()
}

fun newCachedRestBreakEstimateStore(inner: RestBreakEstimateStore, ttl: Int, clock: Clock): RestBreakEstimateStore {
    return CachedRestBreakEstimateStore(
        inner = inner,
        cache = newTtlCache<RestBreakEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break estimates. Empty fields match all. */
data class RestBreakEstimateQuery(
    var depot: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakEstimate>): MutableList<RestBreakEstimate> {
        val matched = filter(items) { candidate: RestBreakEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakEstimates(store: RestBreakEstimateStore, query: RestBreakEstimateQuery): MutableList<RestBreakEstimate> {
    return query.apply(store.list())
}
