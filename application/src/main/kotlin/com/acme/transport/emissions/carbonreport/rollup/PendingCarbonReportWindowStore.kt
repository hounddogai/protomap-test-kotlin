package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending carbon report windows by code. */
interface PendingCarbonReportWindowStore {
    fun find(code: String): PendingCarbonReportWindow?
    fun save(item: PendingCarbonReportWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCarbonReportWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCarbonReportWindowStore(
    var items: MutableMap<String, PendingCarbonReportWindow>,
    var order: MutableList<String>,
) : PendingCarbonReportWindowStore {
    override fun find(code: String): PendingCarbonReportWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCarbonReportWindow) {
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

    override fun list(): MutableList<PendingCarbonReportWindow> {
        val out = mutableListOf<PendingCarbonReportWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCarbonReportWindowStore(): PendingCarbonReportWindowStore {
    return MemoryPendingCarbonReportWindowStore(
        mutableMapOf<String, PendingCarbonReportWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCarbonReportWindowStore(
    var inner: PendingCarbonReportWindowStore,
    var cache: TtlCache<PendingCarbonReportWindow>,
    var hits: Int,
    var misses: Int,
) : PendingCarbonReportWindowStore {
    override fun find(code: String): PendingCarbonReportWindow? {
        val hit = (cache.get(code) as PendingCarbonReportWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCarbonReportWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCarbonReportWindow> = inner.list()
}

fun newCachedPendingCarbonReportWindowStore(inner: PendingCarbonReportWindowStore, ttl: Int, clock: Clock): PendingCarbonReportWindowStore {
    return CachedPendingCarbonReportWindowStore(
        inner = inner,
        cache = newTtlCache<PendingCarbonReportWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending carbon report windows. Empty fields match all. */
data class PendingCarbonReportWindowQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCarbonReportWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PendingCarbonReportWindow>): MutableList<PendingCarbonReportWindow> {
        val matched = filter(items) { candidate: PendingCarbonReportWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCarbonReportWindows(store: PendingCarbonReportWindowStore, query: PendingCarbonReportWindowQuery): MutableList<PendingCarbonReportWindow> {
    return query.apply(store.list())
}
