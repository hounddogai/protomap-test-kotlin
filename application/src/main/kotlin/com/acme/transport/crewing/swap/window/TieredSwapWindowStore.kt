package com.acme.transport.crewing.swap.window

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered swap windows by code. */
interface TieredSwapWindowStore {
    fun find(code: String): TieredSwapWindow?
    fun save(item: TieredSwapWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSwapWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSwapWindowStore(
    var items: MutableMap<String, TieredSwapWindow>,
    var order: MutableList<String>,
) : TieredSwapWindowStore {
    override fun find(code: String): TieredSwapWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSwapWindow) {
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

    override fun list(): MutableList<TieredSwapWindow> {
        val out = mutableListOf<TieredSwapWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSwapWindowStore(): TieredSwapWindowStore {
    return MemoryTieredSwapWindowStore(mutableMapOf<String, TieredSwapWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSwapWindowStore(
    var inner: TieredSwapWindowStore,
    var cache: TtlCache<TieredSwapWindow>,
    var hits: Int,
    var misses: Int,
) : TieredSwapWindowStore {
    override fun find(code: String): TieredSwapWindow? {
        val hit = (cache.get(code) as TieredSwapWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSwapWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSwapWindow> = inner.list()
}

fun newCachedTieredSwapWindowStore(inner: TieredSwapWindowStore, ttl: Int, clock: Clock): TieredSwapWindowStore {
    return CachedTieredSwapWindowStore(
        inner = inner,
        cache = newTtlCache<TieredSwapWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered swap windows. Empty fields match all. */
data class TieredSwapWindowQuery(
    var label: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSwapWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredSwapWindow>): MutableList<TieredSwapWindow> {
        val matched = filter(items) { candidate: TieredSwapWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSwapWindows(store: TieredSwapWindowStore, query: TieredSwapWindowQuery): MutableList<TieredSwapWindow> {
    return query.apply(store.list())
}
