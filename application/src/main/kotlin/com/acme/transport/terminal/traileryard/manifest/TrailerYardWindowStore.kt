package com.acme.transport.terminal.traileryard.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard windows by code. */
interface TrailerYardWindowStore {
    fun find(code: String): TrailerYardWindow?
    fun save(item: TrailerYardWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardWindowStore(
    var items: MutableMap<String, TrailerYardWindow>,
    var order: MutableList<String>,
) : TrailerYardWindowStore {
    override fun find(code: String): TrailerYardWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardWindow) {
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

    override fun list(): MutableList<TrailerYardWindow> {
        val out = mutableListOf<TrailerYardWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardWindowStore(): TrailerYardWindowStore {
    return MemoryTrailerYardWindowStore(mutableMapOf<String, TrailerYardWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardWindowStore(
    var inner: TrailerYardWindowStore,
    var cache: TtlCache<TrailerYardWindow>,
    var hits: Int,
    var misses: Int,
) : TrailerYardWindowStore {
    override fun find(code: String): TrailerYardWindow? {
        val hit = (cache.get(code) as TrailerYardWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardWindow> = inner.list()
}

fun newCachedTrailerYardWindowStore(inner: TrailerYardWindowStore, ttl: Int, clock: Clock): TrailerYardWindowStore {
    return CachedTrailerYardWindowStore(
        inner = inner,
        cache = newTtlCache<TrailerYardWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard windows. Empty fields match all. */
data class TrailerYardWindowQuery(
    var unit: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardWindow): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardWindow>): MutableList<TrailerYardWindow> {
        val matched = filter(items) { candidate: TrailerYardWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardWindows(store: TrailerYardWindowStore, query: TrailerYardWindowQuery): MutableList<TrailerYardWindow> {
    return query.apply(store.list())
}
