package com.acme.transport.customs.inspection.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global inspection thresholds by code. */
interface GlobalInspectionThresholdStore {
    fun find(code: String): GlobalInspectionThreshold?
    fun save(item: GlobalInspectionThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalInspectionThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalInspectionThresholdStore(
    var items: MutableMap<String, GlobalInspectionThreshold>,
    var order: MutableList<String>,
) : GlobalInspectionThresholdStore {
    override fun find(code: String): GlobalInspectionThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalInspectionThreshold) {
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

    override fun list(): MutableList<GlobalInspectionThreshold> {
        val out = mutableListOf<GlobalInspectionThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalInspectionThresholdStore(): GlobalInspectionThresholdStore {
    return MemoryGlobalInspectionThresholdStore(
        mutableMapOf<String, GlobalInspectionThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalInspectionThresholdStore(
    var inner: GlobalInspectionThresholdStore,
    var cache: TtlCache<GlobalInspectionThreshold>,
    var hits: Int,
    var misses: Int,
) : GlobalInspectionThresholdStore {
    override fun find(code: String): GlobalInspectionThreshold? {
        val hit = (cache.get(code) as GlobalInspectionThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalInspectionThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalInspectionThreshold> = inner.list()
}

fun newCachedGlobalInspectionThresholdStore(inner: GlobalInspectionThresholdStore, ttl: Int, clock: Clock): GlobalInspectionThresholdStore {
    return CachedGlobalInspectionThresholdStore(
        inner = inner,
        cache = newTtlCache<GlobalInspectionThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global inspection thresholds. Empty fields match all. */
data class GlobalInspectionThresholdQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalInspectionThreshold): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalInspectionThreshold>): MutableList<GlobalInspectionThreshold> {
        val matched = filter(items) { candidate: GlobalInspectionThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalInspectionThresholds(store: GlobalInspectionThresholdStore, query: GlobalInspectionThresholdQuery): MutableList<GlobalInspectionThreshold> {
    return query.apply(store.list())
}
