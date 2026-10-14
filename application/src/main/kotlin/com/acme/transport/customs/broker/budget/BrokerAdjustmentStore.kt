package com.acme.transport.customs.broker.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker adjustments by code. */
interface BrokerAdjustmentStore {
    fun find(code: String): BrokerAdjustment?
    fun save(item: BrokerAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerAdjustmentStore(
    var items: MutableMap<String, BrokerAdjustment>,
    var order: MutableList<String>,
) : BrokerAdjustmentStore {
    override fun find(code: String): BrokerAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerAdjustment) {
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

    override fun list(): MutableList<BrokerAdjustment> {
        val out = mutableListOf<BrokerAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerAdjustmentStore(): BrokerAdjustmentStore {
    return MemoryBrokerAdjustmentStore(mutableMapOf<String, BrokerAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerAdjustmentStore(
    var inner: BrokerAdjustmentStore,
    var cache: TtlCache<BrokerAdjustment>,
    var hits: Int,
    var misses: Int,
) : BrokerAdjustmentStore {
    override fun find(code: String): BrokerAdjustment? {
        val hit = (cache.get(code) as BrokerAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerAdjustment> = inner.list()
}

fun newCachedBrokerAdjustmentStore(inner: BrokerAdjustmentStore, ttl: Int, clock: Clock): BrokerAdjustmentStore {
    return CachedBrokerAdjustmentStore(
        inner = inner,
        cache = newTtlCache<BrokerAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker adjustments. Empty fields match all. */
data class BrokerAdjustmentQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerAdjustment>): MutableList<BrokerAdjustment> {
        val matched = filter(items) { candidate: BrokerAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerAdjustments(store: BrokerAdjustmentStore, query: BrokerAdjustmentQuery): MutableList<BrokerAdjustment> {
    return query.apply(store.list())
}
