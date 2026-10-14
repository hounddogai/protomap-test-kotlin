package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global backhaul thresholds by code. */
interface GlobalBackhaulThresholdStore {
    fun find(code: String): GlobalBackhaulThreshold?
    fun save(item: GlobalBackhaulThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalBackhaulThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalBackhaulThresholdStore(
    var items: MutableMap<String, GlobalBackhaulThreshold>,
    var order: MutableList<String>,
) : GlobalBackhaulThresholdStore {
    override fun find(code: String): GlobalBackhaulThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalBackhaulThreshold) {
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

    override fun list(): MutableList<GlobalBackhaulThreshold> {
        val out = mutableListOf<GlobalBackhaulThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalBackhaulThresholdStore(): GlobalBackhaulThresholdStore {
    return MemoryGlobalBackhaulThresholdStore(
        mutableMapOf<String, GlobalBackhaulThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalBackhaulThresholdStore(
    var inner: GlobalBackhaulThresholdStore,
    var cache: TtlCache<GlobalBackhaulThreshold>,
    var hits: Int,
    var misses: Int,
) : GlobalBackhaulThresholdStore {
    override fun find(code: String): GlobalBackhaulThreshold? {
        val hit = (cache.get(code) as GlobalBackhaulThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalBackhaulThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalBackhaulThreshold> = inner.list()
}

fun newCachedGlobalBackhaulThresholdStore(inner: GlobalBackhaulThresholdStore, ttl: Int, clock: Clock): GlobalBackhaulThresholdStore {
    return CachedGlobalBackhaulThresholdStore(
        inner = inner,
        cache = newTtlCache<GlobalBackhaulThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global backhaul thresholds. Empty fields match all. */
data class GlobalBackhaulThresholdQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalBackhaulThreshold): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<GlobalBackhaulThreshold>): MutableList<GlobalBackhaulThreshold> {
        val matched = filter(items) { candidate: GlobalBackhaulThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalBackhaulThresholds(store: GlobalBackhaulThresholdStore, query: GlobalBackhaulThresholdQuery): MutableList<GlobalBackhaulThreshold> {
    return query.apply(store.list())
}
