package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling zone windows by code. */
interface RollingZoneWindowStore {
    fun find(code: String): RollingZoneWindow?
    fun save(item: RollingZoneWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingZoneWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingZoneWindowStore(
    var items: MutableMap<String, RollingZoneWindow>,
    var order: MutableList<String>,
) : RollingZoneWindowStore {
    override fun find(code: String): RollingZoneWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingZoneWindow) {
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

    override fun list(): MutableList<RollingZoneWindow> {
        val out = mutableListOf<RollingZoneWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingZoneWindowStore(): RollingZoneWindowStore {
    return MemoryRollingZoneWindowStore(mutableMapOf<String, RollingZoneWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingZoneWindowStore(
    var inner: RollingZoneWindowStore,
    var cache: TtlCache<RollingZoneWindow>,
    var hits: Int,
    var misses: Int,
) : RollingZoneWindowStore {
    override fun find(code: String): RollingZoneWindow? {
        val hit = (cache.get(code) as RollingZoneWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingZoneWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingZoneWindow> = inner.list()
}

fun newCachedRollingZoneWindowStore(inner: RollingZoneWindowStore, ttl: Int, clock: Clock): RollingZoneWindowStore {
    return CachedRollingZoneWindowStore(
        inner = inner,
        cache = newTtlCache<RollingZoneWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling zone windows. Empty fields match all. */
data class RollingZoneWindowQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingZoneWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingZoneWindow>): MutableList<RollingZoneWindow> {
        val matched = filter(items) { candidate: RollingZoneWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingZoneWindows(store: RollingZoneWindowStore, query: RollingZoneWindowQuery): MutableList<RollingZoneWindow> {
    return query.apply(store.list())
}
