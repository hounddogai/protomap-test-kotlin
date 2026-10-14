package com.acme.transport.terminal.dockdoor.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door windows by code. */
interface DockDoorWindowStore {
    fun find(code: String): DockDoorWindow?
    fun save(item: DockDoorWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorWindowStore(
    var items: MutableMap<String, DockDoorWindow>,
    var order: MutableList<String>,
) : DockDoorWindowStore {
    override fun find(code: String): DockDoorWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorWindow) {
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

    override fun list(): MutableList<DockDoorWindow> {
        val out = mutableListOf<DockDoorWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorWindowStore(): DockDoorWindowStore {
    return MemoryDockDoorWindowStore(mutableMapOf<String, DockDoorWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorWindowStore(
    var inner: DockDoorWindowStore,
    var cache: TtlCache<DockDoorWindow>,
    var hits: Int,
    var misses: Int,
) : DockDoorWindowStore {
    override fun find(code: String): DockDoorWindow? {
        val hit = (cache.get(code) as DockDoorWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorWindow> = inner.list()
}

fun newCachedDockDoorWindowStore(inner: DockDoorWindowStore, ttl: Int, clock: Clock): DockDoorWindowStore {
    return CachedDockDoorWindowStore(
        inner = inner,
        cache = newTtlCache<DockDoorWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door windows. Empty fields match all. */
data class DockDoorWindowQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorWindow>): MutableList<DockDoorWindow> {
        val matched = filter(items) { candidate: DockDoorWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorWindows(store: DockDoorWindowStore, query: DockDoorWindowQuery): MutableList<DockDoorWindow> {
    return query.apply(store.list())
}
