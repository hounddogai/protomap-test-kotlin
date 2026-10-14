package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock windows by code. */
interface CrossDockWindowStore {
    fun find(code: String): CrossDockWindow?
    fun save(item: CrossDockWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockWindowStore(
    var items: MutableMap<String, CrossDockWindow>,
    var order: MutableList<String>,
) : CrossDockWindowStore {
    override fun find(code: String): CrossDockWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockWindow) {
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

    override fun list(): MutableList<CrossDockWindow> {
        val out = mutableListOf<CrossDockWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockWindowStore(): CrossDockWindowStore {
    return MemoryCrossDockWindowStore(mutableMapOf<String, CrossDockWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockWindowStore(
    var inner: CrossDockWindowStore,
    var cache: TtlCache<CrossDockWindow>,
    var hits: Int,
    var misses: Int,
) : CrossDockWindowStore {
    override fun find(code: String): CrossDockWindow? {
        val hit = (cache.get(code) as CrossDockWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockWindow> = inner.list()
}

fun newCachedCrossDockWindowStore(inner: CrossDockWindowStore, ttl: Int, clock: Clock): CrossDockWindowStore {
    return CachedCrossDockWindowStore(
        inner = inner,
        cache = newTtlCache<CrossDockWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock windows. Empty fields match all. */
data class CrossDockWindowQuery(
    var lane: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockWindow): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<CrossDockWindow>): MutableList<CrossDockWindow> {
        val matched = filter(items) { candidate: CrossDockWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockWindows(store: CrossDockWindowStore, query: CrossDockWindowQuery): MutableList<CrossDockWindow> {
    return query.apply(store.list())
}
