package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted lane plan windows by code. */
interface WeightedLanePlanWindowStore {
    fun find(code: String): WeightedLanePlanWindow?
    fun save(item: WeightedLanePlanWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLanePlanWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLanePlanWindowStore(
    var items: MutableMap<String, WeightedLanePlanWindow>,
    var order: MutableList<String>,
) : WeightedLanePlanWindowStore {
    override fun find(code: String): WeightedLanePlanWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLanePlanWindow) {
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

    override fun list(): MutableList<WeightedLanePlanWindow> {
        val out = mutableListOf<WeightedLanePlanWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLanePlanWindowStore(): WeightedLanePlanWindowStore {
    return MemoryWeightedLanePlanWindowStore(
        mutableMapOf<String, WeightedLanePlanWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLanePlanWindowStore(
    var inner: WeightedLanePlanWindowStore,
    var cache: TtlCache<WeightedLanePlanWindow>,
    var hits: Int,
    var misses: Int,
) : WeightedLanePlanWindowStore {
    override fun find(code: String): WeightedLanePlanWindow? {
        val hit = (cache.get(code) as WeightedLanePlanWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLanePlanWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLanePlanWindow> = inner.list()
}

fun newCachedWeightedLanePlanWindowStore(inner: WeightedLanePlanWindowStore, ttl: Int, clock: Clock): WeightedLanePlanWindowStore {
    return CachedWeightedLanePlanWindowStore(
        inner = inner,
        cache = newTtlCache<WeightedLanePlanWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lane plan windows. Empty fields match all. */
data class WeightedLanePlanWindowQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLanePlanWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLanePlanWindow>): MutableList<WeightedLanePlanWindow> {
        val matched = filter(items) { candidate: WeightedLanePlanWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLanePlanWindows(store: WeightedLanePlanWindowStore, query: WeightedLanePlanWindowQuery): MutableList<WeightedLanePlanWindow> {
    return query.apply(store.list())
}
