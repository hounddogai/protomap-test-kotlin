package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default fuel surcharge windows by code. */
interface DefaultFuelSurchargeWindowStore {
    fun find(code: String): DefaultFuelSurchargeWindow?
    fun save(item: DefaultFuelSurchargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultFuelSurchargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultFuelSurchargeWindowStore(
    var items: MutableMap<String, DefaultFuelSurchargeWindow>,
    var order: MutableList<String>,
) : DefaultFuelSurchargeWindowStore {
    override fun find(code: String): DefaultFuelSurchargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultFuelSurchargeWindow) {
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

    override fun list(): MutableList<DefaultFuelSurchargeWindow> {
        val out = mutableListOf<DefaultFuelSurchargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultFuelSurchargeWindowStore(): DefaultFuelSurchargeWindowStore {
    return MemoryDefaultFuelSurchargeWindowStore(
        mutableMapOf<String, DefaultFuelSurchargeWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultFuelSurchargeWindowStore(
    var inner: DefaultFuelSurchargeWindowStore,
    var cache: TtlCache<DefaultFuelSurchargeWindow>,
    var hits: Int,
    var misses: Int,
) : DefaultFuelSurchargeWindowStore {
    override fun find(code: String): DefaultFuelSurchargeWindow? {
        val hit = (cache.get(code) as DefaultFuelSurchargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultFuelSurchargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultFuelSurchargeWindow> = inner.list()
}

fun newCachedDefaultFuelSurchargeWindowStore(inner: DefaultFuelSurchargeWindowStore, ttl: Int, clock: Clock): DefaultFuelSurchargeWindowStore {
    return CachedDefaultFuelSurchargeWindowStore(
        inner = inner,
        cache = newTtlCache<DefaultFuelSurchargeWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default fuel surcharge windows. Empty fields match all. */
data class DefaultFuelSurchargeWindowQuery(
    var segment: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultFuelSurchargeWindow): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultFuelSurchargeWindow>): MutableList<DefaultFuelSurchargeWindow> {
        val matched = filter(items) { candidate: DefaultFuelSurchargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultFuelSurchargeWindows(store: DefaultFuelSurchargeWindowStore, query: DefaultFuelSurchargeWindowQuery): MutableList<DefaultFuelSurchargeWindow> {
    return query.apply(store.list())
}
