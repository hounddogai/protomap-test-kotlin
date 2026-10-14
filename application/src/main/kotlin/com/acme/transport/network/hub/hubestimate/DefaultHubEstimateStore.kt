package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default hub estimates by code. */
interface DefaultHubEstimateStore {
    fun find(code: String): DefaultHubEstimate?
    fun save(item: DefaultHubEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultHubEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultHubEstimateStore(
    var items: MutableMap<String, DefaultHubEstimate>,
    var order: MutableList<String>,
) : DefaultHubEstimateStore {
    override fun find(code: String): DefaultHubEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultHubEstimate) {
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

    override fun list(): MutableList<DefaultHubEstimate> {
        val out = mutableListOf<DefaultHubEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultHubEstimateStore(): DefaultHubEstimateStore {
    return MemoryDefaultHubEstimateStore(mutableMapOf<String, DefaultHubEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultHubEstimateStore(
    var inner: DefaultHubEstimateStore,
    var cache: TtlCache<DefaultHubEstimate>,
    var hits: Int,
    var misses: Int,
) : DefaultHubEstimateStore {
    override fun find(code: String): DefaultHubEstimate? {
        val hit = (cache.get(code) as DefaultHubEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultHubEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultHubEstimate> = inner.list()
}

fun newCachedDefaultHubEstimateStore(inner: DefaultHubEstimateStore, ttl: Int, clock: Clock): DefaultHubEstimateStore {
    return CachedDefaultHubEstimateStore(
        inner = inner,
        cache = newTtlCache<DefaultHubEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default hub estimates. Empty fields match all. */
data class DefaultHubEstimateQuery(
    var country: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultHubEstimate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultHubEstimate>): MutableList<DefaultHubEstimate> {
        val matched = filter(items) { candidate: DefaultHubEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultHubEstimates(store: DefaultHubEstimateStore, query: DefaultHubEstimateQuery): MutableList<DefaultHubEstimate> {
    return query.apply(store.list())
}
