package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary relay windows by code. */
interface PrimaryRelayWindowStore {
    fun find(code: String): PrimaryRelayWindow?
    fun save(item: PrimaryRelayWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRelayWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRelayWindowStore(
    var items: MutableMap<String, PrimaryRelayWindow>,
    var order: MutableList<String>,
) : PrimaryRelayWindowStore {
    override fun find(code: String): PrimaryRelayWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRelayWindow) {
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

    override fun list(): MutableList<PrimaryRelayWindow> {
        val out = mutableListOf<PrimaryRelayWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRelayWindowStore(): PrimaryRelayWindowStore {
    return MemoryPrimaryRelayWindowStore(mutableMapOf<String, PrimaryRelayWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRelayWindowStore(
    var inner: PrimaryRelayWindowStore,
    var cache: TtlCache<PrimaryRelayWindow>,
    var hits: Int,
    var misses: Int,
) : PrimaryRelayWindowStore {
    override fun find(code: String): PrimaryRelayWindow? {
        val hit = (cache.get(code) as PrimaryRelayWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRelayWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRelayWindow> = inner.list()
}

fun newCachedPrimaryRelayWindowStore(inner: PrimaryRelayWindowStore, ttl: Int, clock: Clock): PrimaryRelayWindowStore {
    return CachedPrimaryRelayWindowStore(
        inner = inner,
        cache = newTtlCache<PrimaryRelayWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary relay windows. Empty fields match all. */
data class PrimaryRelayWindowQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRelayWindow): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<PrimaryRelayWindow>): MutableList<PrimaryRelayWindow> {
        val matched = filter(items) { candidate: PrimaryRelayWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRelayWindows(store: PrimaryRelayWindowStore, query: PrimaryRelayWindowQuery): MutableList<PrimaryRelayWindow> {
    return query.apply(store.list())
}
