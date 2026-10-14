package com.acme.transport.terminal.dockdoor.doorthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted dock door exceptions by code. */
interface WeightedDockDoorExceptionStore {
    fun find(code: String): WeightedDockDoorException?
    fun save(item: WeightedDockDoorException)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDockDoorException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDockDoorExceptionStore(
    var items: MutableMap<String, WeightedDockDoorException>,
    var order: MutableList<String>,
) : WeightedDockDoorExceptionStore {
    override fun find(code: String): WeightedDockDoorException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDockDoorException) {
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

    override fun list(): MutableList<WeightedDockDoorException> {
        val out = mutableListOf<WeightedDockDoorException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDockDoorExceptionStore(): WeightedDockDoorExceptionStore {
    return MemoryWeightedDockDoorExceptionStore(
        mutableMapOf<String, WeightedDockDoorException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDockDoorExceptionStore(
    var inner: WeightedDockDoorExceptionStore,
    var cache: TtlCache<WeightedDockDoorException>,
    var hits: Int,
    var misses: Int,
) : WeightedDockDoorExceptionStore {
    override fun find(code: String): WeightedDockDoorException? {
        val hit = (cache.get(code) as WeightedDockDoorException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDockDoorException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDockDoorException> = inner.list()
}

fun newCachedWeightedDockDoorExceptionStore(inner: WeightedDockDoorExceptionStore, ttl: Int, clock: Clock): WeightedDockDoorExceptionStore {
    return CachedWeightedDockDoorExceptionStore(
        inner = inner,
        cache = newTtlCache<WeightedDockDoorException>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dock door exceptions. Empty fields match all. */
data class WeightedDockDoorExceptionQuery(
    var locale: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDockDoorException): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<WeightedDockDoorException>): MutableList<WeightedDockDoorException> {
        val matched = filter(items) { candidate: WeightedDockDoorException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDockDoorExceptions(store: WeightedDockDoorExceptionStore, query: WeightedDockDoorExceptionQuery): MutableList<WeightedDockDoorException> {
    return query.apply(store.list())
}
