package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted camera event tiers by code. */
interface WeightedCameraEventTierStore {
    fun find(code: String): WeightedCameraEventTier?
    fun save(item: WeightedCameraEventTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCameraEventTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCameraEventTierStore(
    var items: MutableMap<String, WeightedCameraEventTier>,
    var order: MutableList<String>,
) : WeightedCameraEventTierStore {
    override fun find(code: String): WeightedCameraEventTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCameraEventTier) {
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

    override fun list(): MutableList<WeightedCameraEventTier> {
        val out = mutableListOf<WeightedCameraEventTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCameraEventTierStore(): WeightedCameraEventTierStore {
    return MemoryWeightedCameraEventTierStore(
        mutableMapOf<String, WeightedCameraEventTier>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCameraEventTierStore(
    var inner: WeightedCameraEventTierStore,
    var cache: TtlCache<WeightedCameraEventTier>,
    var hits: Int,
    var misses: Int,
) : WeightedCameraEventTierStore {
    override fun find(code: String): WeightedCameraEventTier? {
        val hit = (cache.get(code) as WeightedCameraEventTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCameraEventTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCameraEventTier> = inner.list()
}

fun newCachedWeightedCameraEventTierStore(inner: WeightedCameraEventTierStore, ttl: Int, clock: Clock): WeightedCameraEventTierStore {
    return CachedWeightedCameraEventTierStore(
        inner = inner,
        cache = newTtlCache<WeightedCameraEventTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted camera event tiers. Empty fields match all. */
data class WeightedCameraEventTierQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCameraEventTier): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCameraEventTier>): MutableList<WeightedCameraEventTier> {
        val matched = filter(items) { candidate: WeightedCameraEventTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCameraEventTiers(store: WeightedCameraEventTierStore, query: WeightedCameraEventTierQuery): MutableList<WeightedCameraEventTier> {
    return query.apply(store.list())
}
