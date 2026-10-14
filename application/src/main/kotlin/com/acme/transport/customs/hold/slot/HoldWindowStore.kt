package com.acme.transport.customs.hold.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold windows by code. */
interface HoldWindowStore {
    fun find(code: String): HoldWindow?
    fun save(item: HoldWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldWindowStore(
    var items: MutableMap<String, HoldWindow>,
    var order: MutableList<String>,
) : HoldWindowStore {
    override fun find(code: String): HoldWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldWindow) {
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

    override fun list(): MutableList<HoldWindow> {
        val out = mutableListOf<HoldWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldWindowStore(): HoldWindowStore {
    return MemoryHoldWindowStore(mutableMapOf<String, HoldWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldWindowStore(
    var inner: HoldWindowStore,
    var cache: TtlCache<HoldWindow>,
    var hits: Int,
    var misses: Int,
) : HoldWindowStore {
    override fun find(code: String): HoldWindow? {
        val hit = (cache.get(code) as HoldWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldWindow> = inner.list()
}

fun newCachedHoldWindowStore(inner: HoldWindowStore, ttl: Int, clock: Clock): HoldWindowStore {
    return CachedHoldWindowStore(
        inner = inner,
        cache = newTtlCache<HoldWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold windows. Empty fields match all. */
data class HoldWindowQuery(
    var source: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldWindow>): MutableList<HoldWindow> {
        val matched = filter(items) { candidate: HoldWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldWindows(store: HoldWindowStore, query: HoldWindowQuery): MutableList<HoldWindow> {
    return query.apply(store.list())
}
