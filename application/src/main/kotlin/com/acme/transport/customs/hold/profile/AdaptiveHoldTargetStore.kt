package com.acme.transport.customs.hold.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive hold targets by code. */
interface AdaptiveHoldTargetStore {
    fun find(code: String): AdaptiveHoldTarget?
    fun save(item: AdaptiveHoldTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveHoldTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveHoldTargetStore(
    var items: MutableMap<String, AdaptiveHoldTarget>,
    var order: MutableList<String>,
) : AdaptiveHoldTargetStore {
    override fun find(code: String): AdaptiveHoldTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveHoldTarget) {
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

    override fun list(): MutableList<AdaptiveHoldTarget> {
        val out = mutableListOf<AdaptiveHoldTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveHoldTargetStore(): AdaptiveHoldTargetStore {
    return MemoryAdaptiveHoldTargetStore(mutableMapOf<String, AdaptiveHoldTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveHoldTargetStore(
    var inner: AdaptiveHoldTargetStore,
    var cache: TtlCache<AdaptiveHoldTarget>,
    var hits: Int,
    var misses: Int,
) : AdaptiveHoldTargetStore {
    override fun find(code: String): AdaptiveHoldTarget? {
        val hit = (cache.get(code) as AdaptiveHoldTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveHoldTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveHoldTarget> = inner.list()
}

fun newCachedAdaptiveHoldTargetStore(inner: AdaptiveHoldTargetStore, ttl: Int, clock: Clock): AdaptiveHoldTargetStore {
    return CachedAdaptiveHoldTargetStore(
        inner = inner,
        cache = newTtlCache<AdaptiveHoldTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive hold targets. Empty fields match all. */
data class AdaptiveHoldTargetQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveHoldTarget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<AdaptiveHoldTarget>): MutableList<AdaptiveHoldTarget> {
        val matched = filter(items) { candidate: AdaptiveHoldTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveHoldTargets(store: AdaptiveHoldTargetStore, query: AdaptiveHoldTargetQuery): MutableList<AdaptiveHoldTarget> {
    return query.apply(store.list())
}
