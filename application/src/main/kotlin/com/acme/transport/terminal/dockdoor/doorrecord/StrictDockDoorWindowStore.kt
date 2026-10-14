package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dock door windows by code. */
interface StrictDockDoorWindowStore {
    fun find(code: String): StrictDockDoorWindow?
    fun save(item: StrictDockDoorWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDockDoorWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDockDoorWindowStore(
    var items: MutableMap<String, StrictDockDoorWindow>,
    var order: MutableList<String>,
) : StrictDockDoorWindowStore {
    override fun find(code: String): StrictDockDoorWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDockDoorWindow) {
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

    override fun list(): MutableList<StrictDockDoorWindow> {
        val out = mutableListOf<StrictDockDoorWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDockDoorWindowStore(): StrictDockDoorWindowStore {
    return MemoryStrictDockDoorWindowStore(
        mutableMapOf<String, StrictDockDoorWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDockDoorWindowStore(
    var inner: StrictDockDoorWindowStore,
    var cache: TtlCache<StrictDockDoorWindow>,
    var hits: Int,
    var misses: Int,
) : StrictDockDoorWindowStore {
    override fun find(code: String): StrictDockDoorWindow? {
        val hit = (cache.get(code) as StrictDockDoorWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDockDoorWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDockDoorWindow> = inner.list()
}

fun newCachedStrictDockDoorWindowStore(inner: StrictDockDoorWindowStore, ttl: Int, clock: Clock): StrictDockDoorWindowStore {
    return CachedStrictDockDoorWindowStore(
        inner = inner,
        cache = newTtlCache<StrictDockDoorWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dock door windows. Empty fields match all. */
data class StrictDockDoorWindowQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDockDoorWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDockDoorWindow>): MutableList<StrictDockDoorWindow> {
        val matched = filter(items) { candidate: StrictDockDoorWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDockDoorWindows(store: StrictDockDoorWindowStore, query: StrictDockDoorWindowQuery): MutableList<StrictDockDoorWindow> {
    return query.apply(store.list())
}
