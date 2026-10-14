package com.acme.transport.customs.inspection.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted inspection bundles by code. */
interface WeightedInspectionBundleStore {
    fun find(code: String): WeightedInspectionBundle?
    fun save(item: WeightedInspectionBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedInspectionBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedInspectionBundleStore(
    var items: MutableMap<String, WeightedInspectionBundle>,
    var order: MutableList<String>,
) : WeightedInspectionBundleStore {
    override fun find(code: String): WeightedInspectionBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedInspectionBundle) {
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

    override fun list(): MutableList<WeightedInspectionBundle> {
        val out = mutableListOf<WeightedInspectionBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedInspectionBundleStore(): WeightedInspectionBundleStore {
    return MemoryWeightedInspectionBundleStore(
        mutableMapOf<String, WeightedInspectionBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedInspectionBundleStore(
    var inner: WeightedInspectionBundleStore,
    var cache: TtlCache<WeightedInspectionBundle>,
    var hits: Int,
    var misses: Int,
) : WeightedInspectionBundleStore {
    override fun find(code: String): WeightedInspectionBundle? {
        val hit = (cache.get(code) as WeightedInspectionBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedInspectionBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedInspectionBundle> = inner.list()
}

fun newCachedWeightedInspectionBundleStore(inner: WeightedInspectionBundleStore, ttl: Int, clock: Clock): WeightedInspectionBundleStore {
    return CachedWeightedInspectionBundleStore(
        inner = inner,
        cache = newTtlCache<WeightedInspectionBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted inspection bundles. Empty fields match all. */
data class WeightedInspectionBundleQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedInspectionBundle): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedInspectionBundle>): MutableList<WeightedInspectionBundle> {
        val matched = filter(items) { candidate: WeightedInspectionBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedInspectionBundles(store: WeightedInspectionBundleStore, query: WeightedInspectionBundleQuery): MutableList<WeightedInspectionBundle> {
    return query.apply(store.list())
}
