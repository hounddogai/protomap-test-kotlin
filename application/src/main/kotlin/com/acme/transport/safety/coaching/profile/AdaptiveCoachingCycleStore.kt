package com.acme.transport.safety.coaching.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive coaching cycles by code. */
interface AdaptiveCoachingCycleStore {
    fun find(code: String): AdaptiveCoachingCycle?
    fun save(item: AdaptiveCoachingCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCoachingCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCoachingCycleStore(
    var items: MutableMap<String, AdaptiveCoachingCycle>,
    var order: MutableList<String>,
) : AdaptiveCoachingCycleStore {
    override fun find(code: String): AdaptiveCoachingCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCoachingCycle) {
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

    override fun list(): MutableList<AdaptiveCoachingCycle> {
        val out = mutableListOf<AdaptiveCoachingCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCoachingCycleStore(): AdaptiveCoachingCycleStore {
    return MemoryAdaptiveCoachingCycleStore(
        mutableMapOf<String, AdaptiveCoachingCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCoachingCycleStore(
    var inner: AdaptiveCoachingCycleStore,
    var cache: TtlCache<AdaptiveCoachingCycle>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCoachingCycleStore {
    override fun find(code: String): AdaptiveCoachingCycle? {
        val hit = (cache.get(code) as AdaptiveCoachingCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCoachingCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCoachingCycle> = inner.list()
}

fun newCachedAdaptiveCoachingCycleStore(inner: AdaptiveCoachingCycleStore, ttl: Int, clock: Clock): AdaptiveCoachingCycleStore {
    return CachedAdaptiveCoachingCycleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCoachingCycle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive coaching cycles. Empty fields match all. */
data class AdaptiveCoachingCycleQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCoachingCycle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCoachingCycle>): MutableList<AdaptiveCoachingCycle> {
        val matched = filter(items) { candidate: AdaptiveCoachingCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCoachingCycles(store: AdaptiveCoachingCycleStore, query: AdaptiveCoachingCycleQuery): MutableList<AdaptiveCoachingCycle> {
    return query.apply(store.list())
}
