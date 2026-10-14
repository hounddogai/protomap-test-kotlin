package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted parcel targets by code. */
interface WeightedParcelTargetStore {
    fun find(code: String): WeightedParcelTarget?
    fun save(item: WeightedParcelTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedParcelTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedParcelTargetStore(
    var items: MutableMap<String, WeightedParcelTarget>,
    var order: MutableList<String>,
) : WeightedParcelTargetStore {
    override fun find(code: String): WeightedParcelTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedParcelTarget) {
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

    override fun list(): MutableList<WeightedParcelTarget> {
        val out = mutableListOf<WeightedParcelTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedParcelTargetStore(): WeightedParcelTargetStore {
    return MemoryWeightedParcelTargetStore(
        mutableMapOf<String, WeightedParcelTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedParcelTargetStore(
    var inner: WeightedParcelTargetStore,
    var cache: TtlCache<WeightedParcelTarget>,
    var hits: Int,
    var misses: Int,
) : WeightedParcelTargetStore {
    override fun find(code: String): WeightedParcelTarget? {
        val hit = (cache.get(code) as WeightedParcelTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedParcelTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedParcelTarget> = inner.list()
}

fun newCachedWeightedParcelTargetStore(inner: WeightedParcelTargetStore, ttl: Int, clock: Clock): WeightedParcelTargetStore {
    return CachedWeightedParcelTargetStore(
        inner = inner,
        cache = newTtlCache<WeightedParcelTarget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted parcel targets. Empty fields match all. */
data class WeightedParcelTargetQuery(
    var region: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedParcelTarget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<WeightedParcelTarget>): MutableList<WeightedParcelTarget> {
        val matched = filter(items) { candidate: WeightedParcelTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedParcelTargets(store: WeightedParcelTargetStore, query: WeightedParcelTargetQuery): MutableList<WeightedParcelTarget> {
    return query.apply(store.list())
}
