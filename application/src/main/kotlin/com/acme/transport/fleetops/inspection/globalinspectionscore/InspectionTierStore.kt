package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection tiers by code. */
interface InspectionTierStore {
    fun find(code: String): InspectionTier?
    fun save(item: InspectionTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionTierStore(
    var items: MutableMap<String, InspectionTier>,
    var order: MutableList<String>,
) : InspectionTierStore {
    override fun find(code: String): InspectionTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionTier) {
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

    override fun list(): MutableList<InspectionTier> {
        val out = mutableListOf<InspectionTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionTierStore(): InspectionTierStore {
    return MemoryInspectionTierStore(mutableMapOf<String, InspectionTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionTierStore(
    var inner: InspectionTierStore,
    var cache: TtlCache<InspectionTier>,
    var hits: Int,
    var misses: Int,
) : InspectionTierStore {
    override fun find(code: String): InspectionTier? {
        val hit = (cache.get(code) as InspectionTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionTier> = inner.list()
}

fun newCachedInspectionTierStore(inner: InspectionTierStore, ttl: Int, clock: Clock): InspectionTierStore {
    return CachedInspectionTierStore(
        inner = inner,
        cache = newTtlCache<InspectionTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection tiers. Empty fields match all. */
data class InspectionTierQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionTier): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionTier>): MutableList<InspectionTier> {
        val matched = filter(items) { candidate: InspectionTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionTiers(store: InspectionTierStore, query: InspectionTierQuery): MutableList<InspectionTier> {
    return query.apply(store.list())
}
