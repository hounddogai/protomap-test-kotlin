package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection bundles by code. */
interface AdaptiveInspectionBundleStore {
    fun find(code: String): AdaptiveInspectionBundle?
    fun save(item: AdaptiveInspectionBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionBundleStore(
    var items: MutableMap<String, AdaptiveInspectionBundle>,
    var order: MutableList<String>,
) : AdaptiveInspectionBundleStore {
    override fun find(code: String): AdaptiveInspectionBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionBundle) {
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

    override fun list(): MutableList<AdaptiveInspectionBundle> {
        val out = mutableListOf<AdaptiveInspectionBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionBundleStore(): AdaptiveInspectionBundleStore {
    return MemoryAdaptiveInspectionBundleStore(
        mutableMapOf<String, AdaptiveInspectionBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionBundleStore(
    var inner: AdaptiveInspectionBundleStore,
    var cache: TtlCache<AdaptiveInspectionBundle>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionBundleStore {
    override fun find(code: String): AdaptiveInspectionBundle? {
        val hit = (cache.get(code) as AdaptiveInspectionBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionBundle> = inner.list()
}

fun newCachedAdaptiveInspectionBundleStore(inner: AdaptiveInspectionBundleStore, ttl: Int, clock: Clock): AdaptiveInspectionBundleStore {
    return CachedAdaptiveInspectionBundleStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection bundles. Empty fields match all. */
data class AdaptiveInspectionBundleQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionBundle>): MutableList<AdaptiveInspectionBundle> {
        val matched = filter(items) { candidate: AdaptiveInspectionBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionBundles(store: AdaptiveInspectionBundleStore, query: AdaptiveInspectionBundleQuery): MutableList<AdaptiveInspectionBundle> {
    return query.apply(store.list())
}
