package com.acme.transport.fleetops.tire.window

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire windows by code. */
interface TireWindowStore {
    fun find(code: String): TireWindow?
    fun save(item: TireWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireWindowStore(
    var items: MutableMap<String, TireWindow>,
    var order: MutableList<String>,
) : TireWindowStore {
    override fun find(code: String): TireWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireWindow) {
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

    override fun list(): MutableList<TireWindow> {
        val out = mutableListOf<TireWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireWindowStore(): TireWindowStore {
    return MemoryTireWindowStore(mutableMapOf<String, TireWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireWindowStore(
    var inner: TireWindowStore,
    var cache: TtlCache<TireWindow>,
    var hits: Int,
    var misses: Int,
) : TireWindowStore {
    override fun find(code: String): TireWindow? {
        val hit = (cache.get(code) as TireWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireWindow> = inner.list()
}

fun newCachedTireWindowStore(inner: TireWindowStore, ttl: Int, clock: Clock): TireWindowStore {
    return CachedTireWindowStore(
        inner = inner,
        cache = newTtlCache<TireWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire windows. Empty fields match all. */
data class TireWindowQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireWindow>): MutableList<TireWindow> {
        val matched = filter(items) { candidate: TireWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireWindows(store: TireWindowStore, query: TireWindowQuery): MutableList<TireWindow> {
    return query.apply(store.list())
}
