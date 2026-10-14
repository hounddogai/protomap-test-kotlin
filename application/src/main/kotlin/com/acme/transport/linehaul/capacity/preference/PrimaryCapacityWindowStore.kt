package com.acme.transport.linehaul.capacity.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary capacity windows by code. */
interface PrimaryCapacityWindowStore {
    fun find(code: String): PrimaryCapacityWindow?
    fun save(item: PrimaryCapacityWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCapacityWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCapacityWindowStore(
    var items: MutableMap<String, PrimaryCapacityWindow>,
    var order: MutableList<String>,
) : PrimaryCapacityWindowStore {
    override fun find(code: String): PrimaryCapacityWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCapacityWindow) {
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

    override fun list(): MutableList<PrimaryCapacityWindow> {
        val out = mutableListOf<PrimaryCapacityWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCapacityWindowStore(): PrimaryCapacityWindowStore {
    return MemoryPrimaryCapacityWindowStore(
        mutableMapOf<String, PrimaryCapacityWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCapacityWindowStore(
    var inner: PrimaryCapacityWindowStore,
    var cache: TtlCache<PrimaryCapacityWindow>,
    var hits: Int,
    var misses: Int,
) : PrimaryCapacityWindowStore {
    override fun find(code: String): PrimaryCapacityWindow? {
        val hit = (cache.get(code) as PrimaryCapacityWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCapacityWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCapacityWindow> = inner.list()
}

fun newCachedPrimaryCapacityWindowStore(inner: PrimaryCapacityWindowStore, ttl: Int, clock: Clock): PrimaryCapacityWindowStore {
    return CachedPrimaryCapacityWindowStore(
        inner = inner,
        cache = newTtlCache<PrimaryCapacityWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary capacity windows. Empty fields match all. */
data class PrimaryCapacityWindowQuery(
    var segment: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCapacityWindow): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<PrimaryCapacityWindow>): MutableList<PrimaryCapacityWindow> {
        val matched = filter(items) { candidate: PrimaryCapacityWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCapacityWindows(store: PrimaryCapacityWindowStore, query: PrimaryCapacityWindowQuery): MutableList<PrimaryCapacityWindow> {
    return query.apply(store.list())
}
