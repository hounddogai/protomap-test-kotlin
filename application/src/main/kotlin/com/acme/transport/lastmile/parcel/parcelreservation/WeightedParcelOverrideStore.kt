package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted parcel overrides by code. */
interface WeightedParcelOverrideStore {
    fun find(code: String): WeightedParcelOverride?
    fun save(item: WeightedParcelOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedParcelOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedParcelOverrideStore(
    var items: MutableMap<String, WeightedParcelOverride>,
    var order: MutableList<String>,
) : WeightedParcelOverrideStore {
    override fun find(code: String): WeightedParcelOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedParcelOverride) {
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

    override fun list(): MutableList<WeightedParcelOverride> {
        val out = mutableListOf<WeightedParcelOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedParcelOverrideStore(): WeightedParcelOverrideStore {
    return MemoryWeightedParcelOverrideStore(
        mutableMapOf<String, WeightedParcelOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedParcelOverrideStore(
    var inner: WeightedParcelOverrideStore,
    var cache: TtlCache<WeightedParcelOverride>,
    var hits: Int,
    var misses: Int,
) : WeightedParcelOverrideStore {
    override fun find(code: String): WeightedParcelOverride? {
        val hit = (cache.get(code) as WeightedParcelOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedParcelOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedParcelOverride> = inner.list()
}

fun newCachedWeightedParcelOverrideStore(inner: WeightedParcelOverrideStore, ttl: Int, clock: Clock): WeightedParcelOverrideStore {
    return CachedWeightedParcelOverrideStore(
        inner = inner,
        cache = newTtlCache<WeightedParcelOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted parcel overrides. Empty fields match all. */
data class WeightedParcelOverrideQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedParcelOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedParcelOverride>): MutableList<WeightedParcelOverride> {
        val matched = filter(items) { candidate: WeightedParcelOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedParcelOverrides(store: WeightedParcelOverrideStore, query: WeightedParcelOverrideQuery): MutableList<WeightedParcelOverride> {
    return query.apply(store.list())
}
