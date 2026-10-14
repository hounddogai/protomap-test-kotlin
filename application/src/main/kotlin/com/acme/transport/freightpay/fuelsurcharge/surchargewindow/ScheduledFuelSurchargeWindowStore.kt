package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel surcharge windows by code. */
interface ScheduledFuelSurchargeWindowStore {
    fun find(code: String): ScheduledFuelSurchargeWindow?
    fun save(item: ScheduledFuelSurchargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelSurchargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelSurchargeWindowStore(
    var items: MutableMap<String, ScheduledFuelSurchargeWindow>,
    var order: MutableList<String>,
) : ScheduledFuelSurchargeWindowStore {
    override fun find(code: String): ScheduledFuelSurchargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelSurchargeWindow) {
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

    override fun list(): MutableList<ScheduledFuelSurchargeWindow> {
        val out = mutableListOf<ScheduledFuelSurchargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelSurchargeWindowStore(): ScheduledFuelSurchargeWindowStore {
    return MemoryScheduledFuelSurchargeWindowStore(
        mutableMapOf<String, ScheduledFuelSurchargeWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelSurchargeWindowStore(
    var inner: ScheduledFuelSurchargeWindowStore,
    var cache: TtlCache<ScheduledFuelSurchargeWindow>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelSurchargeWindowStore {
    override fun find(code: String): ScheduledFuelSurchargeWindow? {
        val hit = (cache.get(code) as ScheduledFuelSurchargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelSurchargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelSurchargeWindow> = inner.list()
}

fun newCachedScheduledFuelSurchargeWindowStore(inner: ScheduledFuelSurchargeWindowStore, ttl: Int, clock: Clock): ScheduledFuelSurchargeWindowStore {
    return CachedScheduledFuelSurchargeWindowStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelSurchargeWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel surcharge windows. Empty fields match all. */
data class ScheduledFuelSurchargeWindowQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelSurchargeWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFuelSurchargeWindow>): MutableList<ScheduledFuelSurchargeWindow> {
        val matched = filter(items) { candidate: ScheduledFuelSurchargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelSurchargeWindows(store: ScheduledFuelSurchargeWindowStore, query: ScheduledFuelSurchargeWindowQuery): MutableList<ScheduledFuelSurchargeWindow> {
    return query.apply(store.list())
}
