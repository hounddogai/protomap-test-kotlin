package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global redelivery estimates by code. */
interface GlobalRedeliveryEstimateStore {
    fun find(code: String): GlobalRedeliveryEstimate?
    fun save(item: GlobalRedeliveryEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRedeliveryEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRedeliveryEstimateStore(
    var items: MutableMap<String, GlobalRedeliveryEstimate>,
    var order: MutableList<String>,
) : GlobalRedeliveryEstimateStore {
    override fun find(code: String): GlobalRedeliveryEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRedeliveryEstimate) {
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

    override fun list(): MutableList<GlobalRedeliveryEstimate> {
        val out = mutableListOf<GlobalRedeliveryEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRedeliveryEstimateStore(): GlobalRedeliveryEstimateStore {
    return MemoryGlobalRedeliveryEstimateStore(
        mutableMapOf<String, GlobalRedeliveryEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRedeliveryEstimateStore(
    var inner: GlobalRedeliveryEstimateStore,
    var cache: TtlCache<GlobalRedeliveryEstimate>,
    var hits: Int,
    var misses: Int,
) : GlobalRedeliveryEstimateStore {
    override fun find(code: String): GlobalRedeliveryEstimate? {
        val hit = (cache.get(code) as GlobalRedeliveryEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRedeliveryEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRedeliveryEstimate> = inner.list()
}

fun newCachedGlobalRedeliveryEstimateStore(inner: GlobalRedeliveryEstimateStore, ttl: Int, clock: Clock): GlobalRedeliveryEstimateStore {
    return CachedGlobalRedeliveryEstimateStore(
        inner = inner,
        cache = newTtlCache<GlobalRedeliveryEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global redelivery estimates. Empty fields match all. */
data class GlobalRedeliveryEstimateQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRedeliveryEstimate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRedeliveryEstimate>): MutableList<GlobalRedeliveryEstimate> {
        val matched = filter(items) { candidate: GlobalRedeliveryEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRedeliveryEstimates(store: GlobalRedeliveryEstimateStore, query: GlobalRedeliveryEstimateQuery): MutableList<GlobalRedeliveryEstimate> {
    return query.apply(store.list())
}
