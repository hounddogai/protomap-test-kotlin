package com.acme.transport.safety.coaching.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted coaching bundles by code. */
interface WeightedCoachingBundleStore {
    fun find(code: String): WeightedCoachingBundle?
    fun save(item: WeightedCoachingBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCoachingBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCoachingBundleStore(
    var items: MutableMap<String, WeightedCoachingBundle>,
    var order: MutableList<String>,
) : WeightedCoachingBundleStore {
    override fun find(code: String): WeightedCoachingBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCoachingBundle) {
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

    override fun list(): MutableList<WeightedCoachingBundle> {
        val out = mutableListOf<WeightedCoachingBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCoachingBundleStore(): WeightedCoachingBundleStore {
    return MemoryWeightedCoachingBundleStore(
        mutableMapOf<String, WeightedCoachingBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCoachingBundleStore(
    var inner: WeightedCoachingBundleStore,
    var cache: TtlCache<WeightedCoachingBundle>,
    var hits: Int,
    var misses: Int,
) : WeightedCoachingBundleStore {
    override fun find(code: String): WeightedCoachingBundle? {
        val hit = (cache.get(code) as WeightedCoachingBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCoachingBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCoachingBundle> = inner.list()
}

fun newCachedWeightedCoachingBundleStore(inner: WeightedCoachingBundleStore, ttl: Int, clock: Clock): WeightedCoachingBundleStore {
    return CachedWeightedCoachingBundleStore(
        inner = inner,
        cache = newTtlCache<WeightedCoachingBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted coaching bundles. Empty fields match all. */
data class WeightedCoachingBundleQuery(
    var title: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCoachingBundle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCoachingBundle>): MutableList<WeightedCoachingBundle> {
        val matched = filter(items) { candidate: WeightedCoachingBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCoachingBundles(store: WeightedCoachingBundleStore, query: WeightedCoachingBundleQuery): MutableList<WeightedCoachingBundle> {
    return query.apply(store.list())
}
