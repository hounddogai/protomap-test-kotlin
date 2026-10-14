package com.acme.transport.customs.clearance.request

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy clearance windows by code. */
interface LegacyClearanceWindowStore {
    fun find(code: String): LegacyClearanceWindow?
    fun save(item: LegacyClearanceWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyClearanceWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyClearanceWindowStore(
    var items: MutableMap<String, LegacyClearanceWindow>,
    var order: MutableList<String>,
) : LegacyClearanceWindowStore {
    override fun find(code: String): LegacyClearanceWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyClearanceWindow) {
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

    override fun list(): MutableList<LegacyClearanceWindow> {
        val out = mutableListOf<LegacyClearanceWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyClearanceWindowStore(): LegacyClearanceWindowStore {
    return MemoryLegacyClearanceWindowStore(
        mutableMapOf<String, LegacyClearanceWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyClearanceWindowStore(
    var inner: LegacyClearanceWindowStore,
    var cache: TtlCache<LegacyClearanceWindow>,
    var hits: Int,
    var misses: Int,
) : LegacyClearanceWindowStore {
    override fun find(code: String): LegacyClearanceWindow? {
        val hit = (cache.get(code) as LegacyClearanceWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyClearanceWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyClearanceWindow> = inner.list()
}

fun newCachedLegacyClearanceWindowStore(inner: LegacyClearanceWindowStore, ttl: Int, clock: Clock): LegacyClearanceWindowStore {
    return CachedLegacyClearanceWindowStore(
        inner = inner,
        cache = newTtlCache<LegacyClearanceWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy clearance windows. Empty fields match all. */
data class LegacyClearanceWindowQuery(
    var label: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyClearanceWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyClearanceWindow>): MutableList<LegacyClearanceWindow> {
        val matched = filter(items) { candidate: LegacyClearanceWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyClearanceWindows(store: LegacyClearanceWindowStore, query: LegacyClearanceWindowQuery): MutableList<LegacyClearanceWindow> {
    return query.apply(store.list())
}
