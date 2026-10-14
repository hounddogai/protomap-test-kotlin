package com.acme.transport.terminal.sortation.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted sortation bundles by code. */
interface WeightedSortationBundleStore {
    fun find(code: String): WeightedSortationBundle?
    fun save(item: WeightedSortationBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedSortationBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedSortationBundleStore(
    var items: MutableMap<String, WeightedSortationBundle>,
    var order: MutableList<String>,
) : WeightedSortationBundleStore {
    override fun find(code: String): WeightedSortationBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedSortationBundle) {
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

    override fun list(): MutableList<WeightedSortationBundle> {
        val out = mutableListOf<WeightedSortationBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedSortationBundleStore(): WeightedSortationBundleStore {
    return MemoryWeightedSortationBundleStore(
        mutableMapOf<String, WeightedSortationBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedSortationBundleStore(
    var inner: WeightedSortationBundleStore,
    var cache: TtlCache<WeightedSortationBundle>,
    var hits: Int,
    var misses: Int,
) : WeightedSortationBundleStore {
    override fun find(code: String): WeightedSortationBundle? {
        val hit = (cache.get(code) as WeightedSortationBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedSortationBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedSortationBundle> = inner.list()
}

fun newCachedWeightedSortationBundleStore(inner: WeightedSortationBundleStore, ttl: Int, clock: Clock): WeightedSortationBundleStore {
    return CachedWeightedSortationBundleStore(
        inner = inner,
        cache = newTtlCache<WeightedSortationBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted sortation bundles. Empty fields match all. */
data class WeightedSortationBundleQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedSortationBundle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedSortationBundle>): MutableList<WeightedSortationBundle> {
        val matched = filter(items) { candidate: WeightedSortationBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedSortationBundles(store: WeightedSortationBundleStore, query: WeightedSortationBundleQuery): MutableList<WeightedSortationBundle> {
    return query.apply(store.list())
}
