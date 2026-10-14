package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending fuel burn windows by code. */
interface PendingFuelBurnWindowStore {
    fun find(code: String): PendingFuelBurnWindow?
    fun save(item: PendingFuelBurnWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFuelBurnWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFuelBurnWindowStore(
    var items: MutableMap<String, PendingFuelBurnWindow>,
    var order: MutableList<String>,
) : PendingFuelBurnWindowStore {
    override fun find(code: String): PendingFuelBurnWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFuelBurnWindow) {
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

    override fun list(): MutableList<PendingFuelBurnWindow> {
        val out = mutableListOf<PendingFuelBurnWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFuelBurnWindowStore(): PendingFuelBurnWindowStore {
    return MemoryPendingFuelBurnWindowStore(
        mutableMapOf<String, PendingFuelBurnWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFuelBurnWindowStore(
    var inner: PendingFuelBurnWindowStore,
    var cache: TtlCache<PendingFuelBurnWindow>,
    var hits: Int,
    var misses: Int,
) : PendingFuelBurnWindowStore {
    override fun find(code: String): PendingFuelBurnWindow? {
        val hit = (cache.get(code) as PendingFuelBurnWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFuelBurnWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFuelBurnWindow> = inner.list()
}

fun newCachedPendingFuelBurnWindowStore(inner: PendingFuelBurnWindowStore, ttl: Int, clock: Clock): PendingFuelBurnWindowStore {
    return CachedPendingFuelBurnWindowStore(
        inner = inner,
        cache = newTtlCache<PendingFuelBurnWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending fuel burn windows. Empty fields match all. */
data class PendingFuelBurnWindowQuery(
    var unit: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFuelBurnWindow): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingFuelBurnWindow>): MutableList<PendingFuelBurnWindow> {
        val matched = filter(items) { candidate: PendingFuelBurnWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFuelBurnWindows(store: PendingFuelBurnWindowStore, query: PendingFuelBurnWindowQuery): MutableList<PendingFuelBurnWindow> {
    return query.apply(store.list())
}
