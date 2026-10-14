package com.acme.transport.customs.clearance.variance

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance windows by code. */
interface ClearanceWindowStore {
    fun find(code: String): ClearanceWindow?
    fun save(item: ClearanceWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceWindowStore(
    var items: MutableMap<String, ClearanceWindow>,
    var order: MutableList<String>,
) : ClearanceWindowStore {
    override fun find(code: String): ClearanceWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceWindow) {
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

    override fun list(): MutableList<ClearanceWindow> {
        val out = mutableListOf<ClearanceWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceWindowStore(): ClearanceWindowStore {
    return MemoryClearanceWindowStore(mutableMapOf<String, ClearanceWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceWindowStore(
    var inner: ClearanceWindowStore,
    var cache: TtlCache<ClearanceWindow>,
    var hits: Int,
    var misses: Int,
) : ClearanceWindowStore {
    override fun find(code: String): ClearanceWindow? {
        val hit = (cache.get(code) as ClearanceWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceWindow> = inner.list()
}

fun newCachedClearanceWindowStore(inner: ClearanceWindowStore, ttl: Int, clock: Clock): ClearanceWindowStore {
    return CachedClearanceWindowStore(
        inner = inner,
        cache = newTtlCache<ClearanceWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance windows. Empty fields match all. */
data class ClearanceWindowQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceWindow>): MutableList<ClearanceWindow> {
        val matched = filter(items) { candidate: ClearanceWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceWindows(store: ClearanceWindowStore, query: ClearanceWindowQuery): MutableList<ClearanceWindow> {
    return query.apply(store.list())
}
