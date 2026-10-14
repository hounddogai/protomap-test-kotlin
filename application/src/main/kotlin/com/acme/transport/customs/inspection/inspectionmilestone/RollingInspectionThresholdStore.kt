package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling inspection thresholds by code. */
interface RollingInspectionThresholdStore {
    fun find(code: String): RollingInspectionThreshold?
    fun save(item: RollingInspectionThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingInspectionThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingInspectionThresholdStore(
    var items: MutableMap<String, RollingInspectionThreshold>,
    var order: MutableList<String>,
) : RollingInspectionThresholdStore {
    override fun find(code: String): RollingInspectionThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingInspectionThreshold) {
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

    override fun list(): MutableList<RollingInspectionThreshold> {
        val out = mutableListOf<RollingInspectionThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingInspectionThresholdStore(): RollingInspectionThresholdStore {
    return MemoryRollingInspectionThresholdStore(
        mutableMapOf<String, RollingInspectionThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingInspectionThresholdStore(
    var inner: RollingInspectionThresholdStore,
    var cache: TtlCache<RollingInspectionThreshold>,
    var hits: Int,
    var misses: Int,
) : RollingInspectionThresholdStore {
    override fun find(code: String): RollingInspectionThreshold? {
        val hit = (cache.get(code) as RollingInspectionThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingInspectionThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingInspectionThreshold> = inner.list()
}

fun newCachedRollingInspectionThresholdStore(inner: RollingInspectionThresholdStore, ttl: Int, clock: Clock): RollingInspectionThresholdStore {
    return CachedRollingInspectionThresholdStore(
        inner = inner,
        cache = newTtlCache<RollingInspectionThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling inspection thresholds. Empty fields match all. */
data class RollingInspectionThresholdQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingInspectionThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingInspectionThreshold>): MutableList<RollingInspectionThreshold> {
        val matched = filter(items) { candidate: RollingInspectionThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingInspectionThresholds(store: RollingInspectionThresholdStore, query: RollingInspectionThresholdQuery): MutableList<RollingInspectionThreshold> {
    return query.apply(store.list())
}
