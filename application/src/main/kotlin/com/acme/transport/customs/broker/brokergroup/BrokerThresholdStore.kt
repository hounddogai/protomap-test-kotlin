package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker thresholds by code. */
interface BrokerThresholdStore {
    fun find(code: String): BrokerThreshold?
    fun save(item: BrokerThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerThresholdStore(
    var items: MutableMap<String, BrokerThreshold>,
    var order: MutableList<String>,
) : BrokerThresholdStore {
    override fun find(code: String): BrokerThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerThreshold) {
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

    override fun list(): MutableList<BrokerThreshold> {
        val out = mutableListOf<BrokerThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerThresholdStore(): BrokerThresholdStore {
    return MemoryBrokerThresholdStore(mutableMapOf<String, BrokerThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerThresholdStore(
    var inner: BrokerThresholdStore,
    var cache: TtlCache<BrokerThreshold>,
    var hits: Int,
    var misses: Int,
) : BrokerThresholdStore {
    override fun find(code: String): BrokerThreshold? {
        val hit = (cache.get(code) as BrokerThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerThreshold> = inner.list()
}

fun newCachedBrokerThresholdStore(inner: BrokerThresholdStore, ttl: Int, clock: Clock): BrokerThresholdStore {
    return CachedBrokerThresholdStore(
        inner = inner,
        cache = newTtlCache<BrokerThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker thresholds. Empty fields match all. */
data class BrokerThresholdQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerThreshold>): MutableList<BrokerThreshold> {
        val matched = filter(items) { candidate: BrokerThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerThresholds(store: BrokerThresholdStore, query: BrokerThresholdQuery): MutableList<BrokerThreshold> {
    return query.apply(store.list())
}
