package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient inspection windows by code. */
interface LenientInspectionWindowStore {
    fun find(code: String): LenientInspectionWindow?
    fun save(item: LenientInspectionWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientInspectionWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientInspectionWindowStore(
    var items: MutableMap<String, LenientInspectionWindow>,
    var order: MutableList<String>,
) : LenientInspectionWindowStore {
    override fun find(code: String): LenientInspectionWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientInspectionWindow) {
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

    override fun list(): MutableList<LenientInspectionWindow> {
        val out = mutableListOf<LenientInspectionWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientInspectionWindowStore(): LenientInspectionWindowStore {
    return MemoryLenientInspectionWindowStore(
        mutableMapOf<String, LenientInspectionWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientInspectionWindowStore(
    var inner: LenientInspectionWindowStore,
    var cache: TtlCache<LenientInspectionWindow>,
    var hits: Int,
    var misses: Int,
) : LenientInspectionWindowStore {
    override fun find(code: String): LenientInspectionWindow? {
        val hit = (cache.get(code) as LenientInspectionWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientInspectionWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientInspectionWindow> = inner.list()
}

fun newCachedLenientInspectionWindowStore(inner: LenientInspectionWindowStore, ttl: Int, clock: Clock): LenientInspectionWindowStore {
    return CachedLenientInspectionWindowStore(
        inner = inner,
        cache = newTtlCache<LenientInspectionWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient inspection windows. Empty fields match all. */
data class LenientInspectionWindowQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientInspectionWindow): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<LenientInspectionWindow>): MutableList<LenientInspectionWindow> {
        val matched = filter(items) { candidate: LenientInspectionWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientInspectionWindows(store: LenientInspectionWindowStore, query: LenientInspectionWindowQuery): MutableList<LenientInspectionWindow> {
    return query.apply(store.list())
}
