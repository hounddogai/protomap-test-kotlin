package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection rollups by code. */
interface InspectionRollupStore {
    fun find(code: String): InspectionRollup?
    fun save(item: InspectionRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionRollupStore(
    var items: MutableMap<String, InspectionRollup>,
    var order: MutableList<String>,
) : InspectionRollupStore {
    override fun find(code: String): InspectionRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionRollup) {
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

    override fun list(): MutableList<InspectionRollup> {
        val out = mutableListOf<InspectionRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionRollupStore(): InspectionRollupStore {
    return MemoryInspectionRollupStore(mutableMapOf<String, InspectionRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionRollupStore(
    var inner: InspectionRollupStore,
    var cache: TtlCache<InspectionRollup>,
    var hits: Int,
    var misses: Int,
) : InspectionRollupStore {
    override fun find(code: String): InspectionRollup? {
        val hit = (cache.get(code) as InspectionRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionRollup> = inner.list()
}

fun newCachedInspectionRollupStore(inner: InspectionRollupStore, ttl: Int, clock: Clock): InspectionRollupStore {
    return CachedInspectionRollupStore(
        inner = inner,
        cache = newTtlCache<InspectionRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection rollups. Empty fields match all. */
data class InspectionRollupQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionRollup>): MutableList<InspectionRollup> {
        val matched = filter(items) { candidate: InspectionRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionRollups(store: InspectionRollupStore, query: InspectionRollupQuery): MutableList<InspectionRollup> {
    return query.apply(store.list())
}
