package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted ev charge groups by code. */
interface WeightedEvChargeGroupStore {
    fun find(code: String): WeightedEvChargeGroup?
    fun save(item: WeightedEvChargeGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEvChargeGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEvChargeGroupStore(
    var items: MutableMap<String, WeightedEvChargeGroup>,
    var order: MutableList<String>,
) : WeightedEvChargeGroupStore {
    override fun find(code: String): WeightedEvChargeGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEvChargeGroup) {
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

    override fun list(): MutableList<WeightedEvChargeGroup> {
        val out = mutableListOf<WeightedEvChargeGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEvChargeGroupStore(): WeightedEvChargeGroupStore {
    return MemoryWeightedEvChargeGroupStore(
        mutableMapOf<String, WeightedEvChargeGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEvChargeGroupStore(
    var inner: WeightedEvChargeGroupStore,
    var cache: TtlCache<WeightedEvChargeGroup>,
    var hits: Int,
    var misses: Int,
) : WeightedEvChargeGroupStore {
    override fun find(code: String): WeightedEvChargeGroup? {
        val hit = (cache.get(code) as WeightedEvChargeGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEvChargeGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEvChargeGroup> = inner.list()
}

fun newCachedWeightedEvChargeGroupStore(inner: WeightedEvChargeGroupStore, ttl: Int, clock: Clock): WeightedEvChargeGroupStore {
    return CachedWeightedEvChargeGroupStore(
        inner = inner,
        cache = newTtlCache<WeightedEvChargeGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted ev charge groups. Empty fields match all. */
data class WeightedEvChargeGroupQuery(
    var channel: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEvChargeGroup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedEvChargeGroup>): MutableList<WeightedEvChargeGroup> {
        val matched = filter(items) { candidate: WeightedEvChargeGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEvChargeGroups(store: WeightedEvChargeGroupStore, query: WeightedEvChargeGroupQuery): MutableList<WeightedEvChargeGroup> {
    return query.apply(store.list())
}
