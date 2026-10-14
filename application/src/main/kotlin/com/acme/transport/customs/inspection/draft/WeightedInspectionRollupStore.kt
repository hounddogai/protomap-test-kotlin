package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted inspection rollups by code. */
interface WeightedInspectionRollupStore {
    fun find(code: String): WeightedInspectionRollup?
    fun save(item: WeightedInspectionRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedInspectionRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedInspectionRollupStore(
    var items: MutableMap<String, WeightedInspectionRollup>,
    var order: MutableList<String>,
) : WeightedInspectionRollupStore {
    override fun find(code: String): WeightedInspectionRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedInspectionRollup) {
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

    override fun list(): MutableList<WeightedInspectionRollup> {
        val out = mutableListOf<WeightedInspectionRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedInspectionRollupStore(): WeightedInspectionRollupStore {
    return MemoryWeightedInspectionRollupStore(
        mutableMapOf<String, WeightedInspectionRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedInspectionRollupStore(
    var inner: WeightedInspectionRollupStore,
    var cache: TtlCache<WeightedInspectionRollup>,
    var hits: Int,
    var misses: Int,
) : WeightedInspectionRollupStore {
    override fun find(code: String): WeightedInspectionRollup? {
        val hit = (cache.get(code) as WeightedInspectionRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedInspectionRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedInspectionRollup> = inner.list()
}

fun newCachedWeightedInspectionRollupStore(inner: WeightedInspectionRollupStore, ttl: Int, clock: Clock): WeightedInspectionRollupStore {
    return CachedWeightedInspectionRollupStore(
        inner = inner,
        cache = newTtlCache<WeightedInspectionRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted inspection rollups. Empty fields match all. */
data class WeightedInspectionRollupQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedInspectionRollup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedInspectionRollup>): MutableList<WeightedInspectionRollup> {
        val matched = filter(items) { candidate: WeightedInspectionRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedInspectionRollups(store: WeightedInspectionRollupStore, query: WeightedInspectionRollupQuery): MutableList<WeightedInspectionRollup> {
    return query.apply(store.list())
}
