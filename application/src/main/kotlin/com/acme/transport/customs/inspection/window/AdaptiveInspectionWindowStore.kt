package com.acme.transport.customs.inspection.window

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection windows by code. */
interface AdaptiveInspectionWindowStore {
    fun find(code: String): AdaptiveInspectionWindow?
    fun save(item: AdaptiveInspectionWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionWindowStore(
    var items: MutableMap<String, AdaptiveInspectionWindow>,
    var order: MutableList<String>,
) : AdaptiveInspectionWindowStore {
    override fun find(code: String): AdaptiveInspectionWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionWindow) {
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

    override fun list(): MutableList<AdaptiveInspectionWindow> {
        val out = mutableListOf<AdaptiveInspectionWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionWindowStore(): AdaptiveInspectionWindowStore {
    return MemoryAdaptiveInspectionWindowStore(
        mutableMapOf<String, AdaptiveInspectionWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionWindowStore(
    var inner: AdaptiveInspectionWindowStore,
    var cache: TtlCache<AdaptiveInspectionWindow>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionWindowStore {
    override fun find(code: String): AdaptiveInspectionWindow? {
        val hit = (cache.get(code) as AdaptiveInspectionWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionWindow> = inner.list()
}

fun newCachedAdaptiveInspectionWindowStore(inner: AdaptiveInspectionWindowStore, ttl: Int, clock: Clock): AdaptiveInspectionWindowStore {
    return CachedAdaptiveInspectionWindowStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection windows. Empty fields match all. */
data class AdaptiveInspectionWindowQuery(
    var depot: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionWindow): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionWindow>): MutableList<AdaptiveInspectionWindow> {
        val matched = filter(items) { candidate: AdaptiveInspectionWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionWindows(store: AdaptiveInspectionWindowStore, query: AdaptiveInspectionWindowQuery): MutableList<AdaptiveInspectionWindow> {
    return query.apply(store.list())
}
