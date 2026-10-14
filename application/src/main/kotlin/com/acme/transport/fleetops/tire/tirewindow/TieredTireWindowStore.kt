package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered tire windows by code. */
interface TieredTireWindowStore {
    fun find(code: String): TieredTireWindow?
    fun save(item: TieredTireWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTireWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTireWindowStore(
    var items: MutableMap<String, TieredTireWindow>,
    var order: MutableList<String>,
) : TieredTireWindowStore {
    override fun find(code: String): TieredTireWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTireWindow) {
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

    override fun list(): MutableList<TieredTireWindow> {
        val out = mutableListOf<TieredTireWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTireWindowStore(): TieredTireWindowStore {
    return MemoryTieredTireWindowStore(mutableMapOf<String, TieredTireWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTireWindowStore(
    var inner: TieredTireWindowStore,
    var cache: TtlCache<TieredTireWindow>,
    var hits: Int,
    var misses: Int,
) : TieredTireWindowStore {
    override fun find(code: String): TieredTireWindow? {
        val hit = (cache.get(code) as TieredTireWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTireWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTireWindow> = inner.list()
}

fun newCachedTieredTireWindowStore(inner: TieredTireWindowStore, ttl: Int, clock: Clock): TieredTireWindowStore {
    return CachedTieredTireWindowStore(
        inner = inner,
        cache = newTtlCache<TieredTireWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tire windows. Empty fields match all. */
data class TieredTireWindowQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTireWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTireWindow>): MutableList<TieredTireWindow> {
        val matched = filter(items) { candidate: TieredTireWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTireWindows(store: TieredTireWindowStore, query: TieredTireWindowQuery): MutableList<TieredTireWindow> {
    return query.apply(store.list())
}
