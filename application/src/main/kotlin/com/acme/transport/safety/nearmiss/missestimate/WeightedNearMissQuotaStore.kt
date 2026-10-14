package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted near miss quotas by code. */
interface WeightedNearMissQuotaStore {
    fun find(code: String): WeightedNearMissQuota?
    fun save(item: WeightedNearMissQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedNearMissQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedNearMissQuotaStore(
    var items: MutableMap<String, WeightedNearMissQuota>,
    var order: MutableList<String>,
) : WeightedNearMissQuotaStore {
    override fun find(code: String): WeightedNearMissQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedNearMissQuota) {
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

    override fun list(): MutableList<WeightedNearMissQuota> {
        val out = mutableListOf<WeightedNearMissQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedNearMissQuotaStore(): WeightedNearMissQuotaStore {
    return MemoryWeightedNearMissQuotaStore(
        mutableMapOf<String, WeightedNearMissQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedNearMissQuotaStore(
    var inner: WeightedNearMissQuotaStore,
    var cache: TtlCache<WeightedNearMissQuota>,
    var hits: Int,
    var misses: Int,
) : WeightedNearMissQuotaStore {
    override fun find(code: String): WeightedNearMissQuota? {
        val hit = (cache.get(code) as WeightedNearMissQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedNearMissQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedNearMissQuota> = inner.list()
}

fun newCachedWeightedNearMissQuotaStore(inner: WeightedNearMissQuotaStore, ttl: Int, clock: Clock): WeightedNearMissQuotaStore {
    return CachedWeightedNearMissQuotaStore(
        inner = inner,
        cache = newTtlCache<WeightedNearMissQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted near miss quotas. Empty fields match all. */
data class WeightedNearMissQuotaQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedNearMissQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedNearMissQuota>): MutableList<WeightedNearMissQuota> {
        val matched = filter(items) { candidate: WeightedNearMissQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedNearMissQuotas(store: WeightedNearMissQuotaStore, query: WeightedNearMissQuotaQuery): MutableList<WeightedNearMissQuota> {
    return query.apply(store.list())
}
