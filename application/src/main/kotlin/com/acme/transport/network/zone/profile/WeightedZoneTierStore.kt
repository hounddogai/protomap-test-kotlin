package com.acme.transport.network.zone.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted zone tiers by code. */
interface WeightedZoneTierStore {
    fun find(code: String): WeightedZoneTier?
    fun save(item: WeightedZoneTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedZoneTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedZoneTierStore(
    var items: MutableMap<String, WeightedZoneTier>,
    var order: MutableList<String>,
) : WeightedZoneTierStore {
    override fun find(code: String): WeightedZoneTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedZoneTier) {
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

    override fun list(): MutableList<WeightedZoneTier> {
        val out = mutableListOf<WeightedZoneTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedZoneTierStore(): WeightedZoneTierStore {
    return MemoryWeightedZoneTierStore(mutableMapOf<String, WeightedZoneTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedZoneTierStore(
    var inner: WeightedZoneTierStore,
    var cache: TtlCache<WeightedZoneTier>,
    var hits: Int,
    var misses: Int,
) : WeightedZoneTierStore {
    override fun find(code: String): WeightedZoneTier? {
        val hit = (cache.get(code) as WeightedZoneTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedZoneTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedZoneTier> = inner.list()
}

fun newCachedWeightedZoneTierStore(inner: WeightedZoneTierStore, ttl: Int, clock: Clock): WeightedZoneTierStore {
    return CachedWeightedZoneTierStore(
        inner = inner,
        cache = newTtlCache<WeightedZoneTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted zone tiers. Empty fields match all. */
data class WeightedZoneTierQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedZoneTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<WeightedZoneTier>): MutableList<WeightedZoneTier> {
        val matched = filter(items) { candidate: WeightedZoneTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedZoneTiers(store: WeightedZoneTierStore, query: WeightedZoneTierQuery): MutableList<WeightedZoneTier> {
    return query.apply(store.list())
}
