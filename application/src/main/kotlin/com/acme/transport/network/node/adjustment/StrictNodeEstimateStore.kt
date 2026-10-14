package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps strict node estimates by code. */
interface StrictNodeEstimateStore {
    fun find(code: String): StrictNodeEstimate?
    fun save(item: StrictNodeEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictNodeEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictNodeEstimateStore(
    var items: MutableMap<String, StrictNodeEstimate>,
    var order: MutableList<String>,
) : StrictNodeEstimateStore {
    override fun find(code: String): StrictNodeEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictNodeEstimate) {
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

    override fun list(): MutableList<StrictNodeEstimate> {
        val out = mutableListOf<StrictNodeEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictNodeEstimateStore(): StrictNodeEstimateStore {
    return MemoryStrictNodeEstimateStore(mutableMapOf<String, StrictNodeEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictNodeEstimateStore(
    var inner: StrictNodeEstimateStore,
    var cache: TtlCache<StrictNodeEstimate>,
    var hits: Int,
    var misses: Int,
) : StrictNodeEstimateStore {
    override fun find(code: String): StrictNodeEstimate? {
        val hit = (cache.get(code) as StrictNodeEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictNodeEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictNodeEstimate> = inner.list()
}

fun newCachedStrictNodeEstimateStore(inner: StrictNodeEstimateStore, ttl: Int, clock: Clock): StrictNodeEstimateStore {
    return CachedStrictNodeEstimateStore(
        inner = inner,
        cache = newTtlCache<StrictNodeEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict node estimates. Empty fields match all. */
data class StrictNodeEstimateQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictNodeEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictNodeEstimate>): MutableList<StrictNodeEstimate> {
        val matched = filter(items) { candidate: StrictNodeEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictNodeEstimates(store: StrictNodeEstimateStore, query: StrictNodeEstimateQuery): MutableList<StrictNodeEstimate> {
    return query.apply(store.list())
}
