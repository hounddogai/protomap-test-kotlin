package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps manual parcel windows by code. */
interface ManualParcelWindowStore {
    fun find(code: String): ManualParcelWindow?
    fun save(item: ManualParcelWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualParcelWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualParcelWindowStore(
    var items: MutableMap<String, ManualParcelWindow>,
    var order: MutableList<String>,
) : ManualParcelWindowStore {
    override fun find(code: String): ManualParcelWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualParcelWindow) {
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

    override fun list(): MutableList<ManualParcelWindow> {
        val out = mutableListOf<ManualParcelWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualParcelWindowStore(): ManualParcelWindowStore {
    return MemoryManualParcelWindowStore(mutableMapOf<String, ManualParcelWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualParcelWindowStore(
    var inner: ManualParcelWindowStore,
    var cache: TtlCache<ManualParcelWindow>,
    var hits: Int,
    var misses: Int,
) : ManualParcelWindowStore {
    override fun find(code: String): ManualParcelWindow? {
        val hit = (cache.get(code) as ManualParcelWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualParcelWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualParcelWindow> = inner.list()
}

fun newCachedManualParcelWindowStore(inner: ManualParcelWindowStore, ttl: Int, clock: Clock): ManualParcelWindowStore {
    return CachedManualParcelWindowStore(
        inner = inner,
        cache = newTtlCache<ManualParcelWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual parcel windows. Empty fields match all. */
data class ManualParcelWindowQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualParcelWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualParcelWindow>): MutableList<ManualParcelWindow> {
        val matched = filter(items) { candidate: ManualParcelWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualParcelWindows(store: ManualParcelWindowStore, query: ManualParcelWindowQuery): MutableList<ManualParcelWindow> {
    return query.apply(store.list())
}
