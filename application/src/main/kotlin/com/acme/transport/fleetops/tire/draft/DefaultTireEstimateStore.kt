package com.acme.transport.fleetops.tire.draft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default tire estimates by code. */
interface DefaultTireEstimateStore {
    fun find(code: String): DefaultTireEstimate?
    fun save(item: DefaultTireEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTireEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTireEstimateStore(
    var items: MutableMap<String, DefaultTireEstimate>,
    var order: MutableList<String>,
) : DefaultTireEstimateStore {
    override fun find(code: String): DefaultTireEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTireEstimate) {
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

    override fun list(): MutableList<DefaultTireEstimate> {
        val out = mutableListOf<DefaultTireEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTireEstimateStore(): DefaultTireEstimateStore {
    return MemoryDefaultTireEstimateStore(mutableMapOf<String, DefaultTireEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTireEstimateStore(
    var inner: DefaultTireEstimateStore,
    var cache: TtlCache<DefaultTireEstimate>,
    var hits: Int,
    var misses: Int,
) : DefaultTireEstimateStore {
    override fun find(code: String): DefaultTireEstimate? {
        val hit = (cache.get(code) as DefaultTireEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTireEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTireEstimate> = inner.list()
}

fun newCachedDefaultTireEstimateStore(inner: DefaultTireEstimateStore, ttl: Int, clock: Clock): DefaultTireEstimateStore {
    return CachedDefaultTireEstimateStore(
        inner = inner,
        cache = newTtlCache<DefaultTireEstimate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default tire estimates. Empty fields match all. */
data class DefaultTireEstimateQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTireEstimate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTireEstimate>): MutableList<DefaultTireEstimate> {
        val matched = filter(items) { candidate: DefaultTireEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTireEstimates(store: DefaultTireEstimateStore, query: DefaultTireEstimateQuery): MutableList<DefaultTireEstimate> {
    return query.apply(store.list())
}
