package com.acme.transport.network.hub.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling hub estimates by code. */
interface RollingHubEstimateStore {
    fun find(code: String): RollingHubEstimate?
    fun save(item: RollingHubEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingHubEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingHubEstimateStore(
    var items: MutableMap<String, RollingHubEstimate>,
    var order: MutableList<String>,
) : RollingHubEstimateStore {
    override fun find(code: String): RollingHubEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingHubEstimate) {
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

    override fun list(): MutableList<RollingHubEstimate> {
        val out = mutableListOf<RollingHubEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingHubEstimateStore(): RollingHubEstimateStore {
    return MemoryRollingHubEstimateStore(mutableMapOf<String, RollingHubEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingHubEstimateStore(
    var inner: RollingHubEstimateStore,
    var cache: TtlCache<RollingHubEstimate>,
    var hits: Int,
    var misses: Int,
) : RollingHubEstimateStore {
    override fun find(code: String): RollingHubEstimate? {
        val hit = (cache.get(code) as RollingHubEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingHubEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingHubEstimate> = inner.list()
}

fun newCachedRollingHubEstimateStore(inner: RollingHubEstimateStore, ttl: Int, clock: Clock): RollingHubEstimateStore {
    return CachedRollingHubEstimateStore(
        inner = inner,
        cache = newTtlCache<RollingHubEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling hub estimates. Empty fields match all. */
data class RollingHubEstimateQuery(
    var depot: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingHubEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingHubEstimate>): MutableList<RollingHubEstimate> {
        val matched = filter(items) { candidate: RollingHubEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingHubEstimates(store: RollingHubEstimateStore, query: RollingHubEstimateQuery): MutableList<RollingHubEstimate> {
    return query.apply(store.list())
}
