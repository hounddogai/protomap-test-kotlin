package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge estimates by code. */
interface EvChargeEstimateStore {
    fun find(code: String): EvChargeEstimate?
    fun save(item: EvChargeEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeEstimateStore(
    var items: MutableMap<String, EvChargeEstimate>,
    var order: MutableList<String>,
) : EvChargeEstimateStore {
    override fun find(code: String): EvChargeEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeEstimate) {
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

    override fun list(): MutableList<EvChargeEstimate> {
        val out = mutableListOf<EvChargeEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeEstimateStore(): EvChargeEstimateStore {
    return MemoryEvChargeEstimateStore(mutableMapOf<String, EvChargeEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeEstimateStore(
    var inner: EvChargeEstimateStore,
    var cache: TtlCache<EvChargeEstimate>,
    var hits: Int,
    var misses: Int,
) : EvChargeEstimateStore {
    override fun find(code: String): EvChargeEstimate? {
        val hit = (cache.get(code) as EvChargeEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeEstimate> = inner.list()
}

fun newCachedEvChargeEstimateStore(inner: EvChargeEstimateStore, ttl: Int, clock: Clock): EvChargeEstimateStore {
    return CachedEvChargeEstimateStore(
        inner = inner,
        cache = newTtlCache<EvChargeEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge estimates. Empty fields match all. */
data class EvChargeEstimateQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeEstimate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeEstimate>): MutableList<EvChargeEstimate> {
        val matched = filter(items) { candidate: EvChargeEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeEstimates(store: EvChargeEstimateStore, query: EvChargeEstimateQuery): MutableList<EvChargeEstimate> {
    return query.apply(store.list())
}
