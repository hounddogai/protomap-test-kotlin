package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict backhaul windows by code. */
interface StrictBackhaulWindowStore {
    fun find(code: String): StrictBackhaulWindow?
    fun save(item: StrictBackhaulWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBackhaulWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBackhaulWindowStore(
    var items: MutableMap<String, StrictBackhaulWindow>,
    var order: MutableList<String>,
) : StrictBackhaulWindowStore {
    override fun find(code: String): StrictBackhaulWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBackhaulWindow) {
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

    override fun list(): MutableList<StrictBackhaulWindow> {
        val out = mutableListOf<StrictBackhaulWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBackhaulWindowStore(): StrictBackhaulWindowStore {
    return MemoryStrictBackhaulWindowStore(
        mutableMapOf<String, StrictBackhaulWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBackhaulWindowStore(
    var inner: StrictBackhaulWindowStore,
    var cache: TtlCache<StrictBackhaulWindow>,
    var hits: Int,
    var misses: Int,
) : StrictBackhaulWindowStore {
    override fun find(code: String): StrictBackhaulWindow? {
        val hit = (cache.get(code) as StrictBackhaulWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBackhaulWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBackhaulWindow> = inner.list()
}

fun newCachedStrictBackhaulWindowStore(inner: StrictBackhaulWindowStore, ttl: Int, clock: Clock): StrictBackhaulWindowStore {
    return CachedStrictBackhaulWindowStore(
        inner = inner,
        cache = newTtlCache<StrictBackhaulWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict backhaul windows. Empty fields match all. */
data class StrictBackhaulWindowQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBackhaulWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBackhaulWindow>): MutableList<StrictBackhaulWindow> {
        val matched = filter(items) { candidate: StrictBackhaulWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBackhaulWindows(store: StrictBackhaulWindowStore, query: StrictBackhaulWindowQuery): MutableList<StrictBackhaulWindow> {
    return query.apply(store.list())
}
