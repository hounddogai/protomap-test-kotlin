package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted broker groups by code. */
interface WeightedBrokerGroupStore {
    fun find(code: String): WeightedBrokerGroup?
    fun save(item: WeightedBrokerGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedBrokerGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedBrokerGroupStore(
    var items: MutableMap<String, WeightedBrokerGroup>,
    var order: MutableList<String>,
) : WeightedBrokerGroupStore {
    override fun find(code: String): WeightedBrokerGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedBrokerGroup) {
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

    override fun list(): MutableList<WeightedBrokerGroup> {
        val out = mutableListOf<WeightedBrokerGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedBrokerGroupStore(): WeightedBrokerGroupStore {
    return MemoryWeightedBrokerGroupStore(mutableMapOf<String, WeightedBrokerGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedBrokerGroupStore(
    var inner: WeightedBrokerGroupStore,
    var cache: TtlCache<WeightedBrokerGroup>,
    var hits: Int,
    var misses: Int,
) : WeightedBrokerGroupStore {
    override fun find(code: String): WeightedBrokerGroup? {
        val hit = (cache.get(code) as WeightedBrokerGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedBrokerGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedBrokerGroup> = inner.list()
}

fun newCachedWeightedBrokerGroupStore(inner: WeightedBrokerGroupStore, ttl: Int, clock: Clock): WeightedBrokerGroupStore {
    return CachedWeightedBrokerGroupStore(
        inner = inner,
        cache = newTtlCache<WeightedBrokerGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted broker groups. Empty fields match all. */
data class WeightedBrokerGroupQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedBrokerGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedBrokerGroup>): MutableList<WeightedBrokerGroup> {
        val matched = filter(items) { candidate: WeightedBrokerGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedBrokerGroups(store: WeightedBrokerGroupStore, query: WeightedBrokerGroupQuery): MutableList<WeightedBrokerGroup> {
    return query.apply(store.list())
}
