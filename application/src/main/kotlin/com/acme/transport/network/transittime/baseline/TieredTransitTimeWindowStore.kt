package com.acme.transport.network.transittime.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered transit time windows by code. */
interface TieredTransitTimeWindowStore {
    fun find(code: String): TieredTransitTimeWindow?
    fun save(item: TieredTransitTimeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTransitTimeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTransitTimeWindowStore(
    var items: MutableMap<String, TieredTransitTimeWindow>,
    var order: MutableList<String>,
) : TieredTransitTimeWindowStore {
    override fun find(code: String): TieredTransitTimeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTransitTimeWindow) {
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

    override fun list(): MutableList<TieredTransitTimeWindow> {
        val out = mutableListOf<TieredTransitTimeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTransitTimeWindowStore(): TieredTransitTimeWindowStore {
    return MemoryTieredTransitTimeWindowStore(
        mutableMapOf<String, TieredTransitTimeWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTransitTimeWindowStore(
    var inner: TieredTransitTimeWindowStore,
    var cache: TtlCache<TieredTransitTimeWindow>,
    var hits: Int,
    var misses: Int,
) : TieredTransitTimeWindowStore {
    override fun find(code: String): TieredTransitTimeWindow? {
        val hit = (cache.get(code) as TieredTransitTimeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTransitTimeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTransitTimeWindow> = inner.list()
}

fun newCachedTieredTransitTimeWindowStore(inner: TieredTransitTimeWindowStore, ttl: Int, clock: Clock): TieredTransitTimeWindowStore {
    return CachedTieredTransitTimeWindowStore(
        inner = inner,
        cache = newTtlCache<TieredTransitTimeWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered transit time windows. Empty fields match all. */
data class TieredTransitTimeWindowQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTransitTimeWindow): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTransitTimeWindow>): MutableList<TieredTransitTimeWindow> {
        val matched = filter(items) { candidate: TieredTransitTimeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTransitTimeWindows(store: TieredTransitTimeWindowStore, query: TieredTransitTimeWindowQuery): MutableList<TieredTransitTimeWindow> {
    return query.apply(store.list())
}
