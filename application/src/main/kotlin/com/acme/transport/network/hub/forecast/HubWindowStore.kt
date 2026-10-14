package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub windows by code. */
interface HubWindowStore {
    fun find(code: String): HubWindow?
    fun save(item: HubWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubWindowStore(
    var items: MutableMap<String, HubWindow>,
    var order: MutableList<String>,
) : HubWindowStore {
    override fun find(code: String): HubWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubWindow) {
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

    override fun list(): MutableList<HubWindow> {
        val out = mutableListOf<HubWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubWindowStore(): HubWindowStore {
    return MemoryHubWindowStore(mutableMapOf<String, HubWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubWindowStore(
    var inner: HubWindowStore,
    var cache: TtlCache<HubWindow>,
    var hits: Int,
    var misses: Int,
) : HubWindowStore {
    override fun find(code: String): HubWindow? {
        val hit = (cache.get(code) as HubWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubWindow> = inner.list()
}

fun newCachedHubWindowStore(inner: HubWindowStore, ttl: Int, clock: Clock): HubWindowStore {
    return CachedHubWindowStore(
        inner = inner,
        cache = newTtlCache<HubWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub windows. Empty fields match all. */
data class HubWindowQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<HubWindow>): MutableList<HubWindow> {
        val matched = filter(items) { candidate: HubWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubWindows(store: HubWindowStore, query: HubWindowQuery): MutableList<HubWindow> {
    return query.apply(store.list())
}
