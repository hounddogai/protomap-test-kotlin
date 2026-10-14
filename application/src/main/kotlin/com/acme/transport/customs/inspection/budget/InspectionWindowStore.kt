package com.acme.transport.customs.inspection.budget

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection windows by code. */
interface InspectionWindowStore {
    fun find(code: String): InspectionWindow?
    fun save(item: InspectionWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionWindowStore(
    var items: MutableMap<String, InspectionWindow>,
    var order: MutableList<String>,
) : InspectionWindowStore {
    override fun find(code: String): InspectionWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionWindow) {
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

    override fun list(): MutableList<InspectionWindow> {
        val out = mutableListOf<InspectionWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionWindowStore(): InspectionWindowStore {
    return MemoryInspectionWindowStore(mutableMapOf<String, InspectionWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionWindowStore(
    var inner: InspectionWindowStore,
    var cache: TtlCache<InspectionWindow>,
    var hits: Int,
    var misses: Int,
) : InspectionWindowStore {
    override fun find(code: String): InspectionWindow? {
        val hit = (cache.get(code) as InspectionWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionWindow> = inner.list()
}

fun newCachedInspectionWindowStore(inner: InspectionWindowStore, ttl: Int, clock: Clock): InspectionWindowStore {
    return CachedInspectionWindowStore(
        inner = inner,
        cache = newTtlCache<InspectionWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection windows. Empty fields match all. */
data class InspectionWindowQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<InspectionWindow>): MutableList<InspectionWindow> {
        val matched = filter(items) { candidate: InspectionWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionWindows(store: InspectionWindowStore, query: InspectionWindowQuery): MutableList<InspectionWindow> {
    return query.apply(store.list())
}
