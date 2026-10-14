package com.acme.transport.network.cutoff.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff estimates by code. */
interface DefaultCutoffEstimateStore {
    fun find(code: String): DefaultCutoffEstimate?
    fun save(item: DefaultCutoffEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffEstimateStore(
    var items: MutableMap<String, DefaultCutoffEstimate>,
    var order: MutableList<String>,
) : DefaultCutoffEstimateStore {
    override fun find(code: String): DefaultCutoffEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffEstimate) {
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

    override fun list(): MutableList<DefaultCutoffEstimate> {
        val out = mutableListOf<DefaultCutoffEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffEstimateStore(): DefaultCutoffEstimateStore {
    return MemoryDefaultCutoffEstimateStore(
        mutableMapOf<String, DefaultCutoffEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffEstimateStore(
    var inner: DefaultCutoffEstimateStore,
    var cache: TtlCache<DefaultCutoffEstimate>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffEstimateStore {
    override fun find(code: String): DefaultCutoffEstimate? {
        val hit = (cache.get(code) as DefaultCutoffEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffEstimate> = inner.list()
}

fun newCachedDefaultCutoffEstimateStore(inner: DefaultCutoffEstimateStore, ttl: Int, clock: Clock): DefaultCutoffEstimateStore {
    return CachedDefaultCutoffEstimateStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff estimates. Empty fields match all. */
data class DefaultCutoffEstimateQuery(
    var country: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffEstimate>): MutableList<DefaultCutoffEstimate> {
        val matched = filter(items) { candidate: DefaultCutoffEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffEstimates(store: DefaultCutoffEstimateStore, query: DefaultCutoffEstimateQuery): MutableList<DefaultCutoffEstimate> {
    return query.apply(store.list())
}
