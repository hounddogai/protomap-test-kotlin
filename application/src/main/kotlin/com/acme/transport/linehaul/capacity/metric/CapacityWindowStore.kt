package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity windows by code. */
interface CapacityWindowStore {
    fun find(code: String): CapacityWindow?
    fun save(item: CapacityWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityWindowStore(
    var items: MutableMap<String, CapacityWindow>,
    var order: MutableList<String>,
) : CapacityWindowStore {
    override fun find(code: String): CapacityWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityWindow) {
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

    override fun list(): MutableList<CapacityWindow> {
        val out = mutableListOf<CapacityWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityWindowStore(): CapacityWindowStore {
    return MemoryCapacityWindowStore(mutableMapOf<String, CapacityWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityWindowStore(
    var inner: CapacityWindowStore,
    var cache: TtlCache<CapacityWindow>,
    var hits: Int,
    var misses: Int,
) : CapacityWindowStore {
    override fun find(code: String): CapacityWindow? {
        val hit = (cache.get(code) as CapacityWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityWindow> = inner.list()
}

fun newCachedCapacityWindowStore(inner: CapacityWindowStore, ttl: Int, clock: Clock): CapacityWindowStore {
    return CachedCapacityWindowStore(
        inner = inner,
        cache = newTtlCache<CapacityWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity windows. Empty fields match all. */
data class CapacityWindowQuery(
    var unit: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityWindow): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<CapacityWindow>): MutableList<CapacityWindow> {
        val matched = filter(items) { candidate: CapacityWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityWindows(store: CapacityWindowStore, query: CapacityWindowQuery): MutableList<CapacityWindow> {
    return query.apply(store.list())
}
