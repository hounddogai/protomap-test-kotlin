package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional parcel windows by code. */
interface RegionalParcelWindowStore {
    fun find(code: String): RegionalParcelWindow?
    fun save(item: RegionalParcelWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalParcelWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalParcelWindowStore(
    var items: MutableMap<String, RegionalParcelWindow>,
    var order: MutableList<String>,
) : RegionalParcelWindowStore {
    override fun find(code: String): RegionalParcelWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalParcelWindow) {
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

    override fun list(): MutableList<RegionalParcelWindow> {
        val out = mutableListOf<RegionalParcelWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalParcelWindowStore(): RegionalParcelWindowStore {
    return MemoryRegionalParcelWindowStore(
        mutableMapOf<String, RegionalParcelWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalParcelWindowStore(
    var inner: RegionalParcelWindowStore,
    var cache: TtlCache<RegionalParcelWindow>,
    var hits: Int,
    var misses: Int,
) : RegionalParcelWindowStore {
    override fun find(code: String): RegionalParcelWindow? {
        val hit = (cache.get(code) as RegionalParcelWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalParcelWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalParcelWindow> = inner.list()
}

fun newCachedRegionalParcelWindowStore(inner: RegionalParcelWindowStore, ttl: Int, clock: Clock): RegionalParcelWindowStore {
    return CachedRegionalParcelWindowStore(
        inner = inner,
        cache = newTtlCache<RegionalParcelWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional parcel windows. Empty fields match all. */
data class RegionalParcelWindowQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalParcelWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RegionalParcelWindow>): MutableList<RegionalParcelWindow> {
        val matched = filter(items) { candidate: RegionalParcelWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalParcelWindows(store: RegionalParcelWindowStore, query: RegionalParcelWindowQuery): MutableList<RegionalParcelWindow> {
    return query.apply(store.list())
}
