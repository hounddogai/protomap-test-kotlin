package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection thresholds by code. */
interface AdaptiveInspectionThresholdStore {
    fun find(code: String): AdaptiveInspectionThreshold?
    fun save(item: AdaptiveInspectionThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionThresholdStore(
    var items: MutableMap<String, AdaptiveInspectionThreshold>,
    var order: MutableList<String>,
) : AdaptiveInspectionThresholdStore {
    override fun find(code: String): AdaptiveInspectionThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionThreshold) {
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

    override fun list(): MutableList<AdaptiveInspectionThreshold> {
        val out = mutableListOf<AdaptiveInspectionThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionThresholdStore(): AdaptiveInspectionThresholdStore {
    return MemoryAdaptiveInspectionThresholdStore(
        mutableMapOf<String, AdaptiveInspectionThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionThresholdStore(
    var inner: AdaptiveInspectionThresholdStore,
    var cache: TtlCache<AdaptiveInspectionThreshold>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionThresholdStore {
    override fun find(code: String): AdaptiveInspectionThreshold? {
        val hit = (cache.get(code) as AdaptiveInspectionThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionThreshold> = inner.list()
}

fun newCachedAdaptiveInspectionThresholdStore(inner: AdaptiveInspectionThresholdStore, ttl: Int, clock: Clock): AdaptiveInspectionThresholdStore {
    return CachedAdaptiveInspectionThresholdStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection thresholds. Empty fields match all. */
data class AdaptiveInspectionThresholdQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionThreshold>): MutableList<AdaptiveInspectionThreshold> {
        val matched = filter(items) { candidate: AdaptiveInspectionThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionThresholds(store: AdaptiveInspectionThresholdStore, query: AdaptiveInspectionThresholdQuery): MutableList<AdaptiveInspectionThreshold> {
    return query.apply(store.list())
}
