package com.acme.transport.linehaul.relay.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay windows by code. */
interface RelayWindowStore {
    fun find(code: String): RelayWindow?
    fun save(item: RelayWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayWindowStore(
    var items: MutableMap<String, RelayWindow>,
    var order: MutableList<String>,
) : RelayWindowStore {
    override fun find(code: String): RelayWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayWindow) {
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

    override fun list(): MutableList<RelayWindow> {
        val out = mutableListOf<RelayWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayWindowStore(): RelayWindowStore {
    return MemoryRelayWindowStore(mutableMapOf<String, RelayWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayWindowStore(
    var inner: RelayWindowStore,
    var cache: TtlCache<RelayWindow>,
    var hits: Int,
    var misses: Int,
) : RelayWindowStore {
    override fun find(code: String): RelayWindow? {
        val hit = (cache.get(code) as RelayWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayWindow> = inner.list()
}

fun newCachedRelayWindowStore(inner: RelayWindowStore, ttl: Int, clock: Clock): RelayWindowStore {
    return CachedRelayWindowStore(
        inner = inner,
        cache = newTtlCache<RelayWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay windows. Empty fields match all. */
data class RelayWindowQuery(
    var country: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayWindow>): MutableList<RelayWindow> {
        val matched = filter(items) { candidate: RelayWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayWindows(store: RelayWindowStore, query: RelayWindowQuery): MutableList<RelayWindow> {
    return query.apply(store.list())
}
