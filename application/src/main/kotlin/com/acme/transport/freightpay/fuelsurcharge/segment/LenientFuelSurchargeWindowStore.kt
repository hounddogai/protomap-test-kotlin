package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient fuel surcharge windows by code. */
interface LenientFuelSurchargeWindowStore {
    fun find(code: String): LenientFuelSurchargeWindow?
    fun save(item: LenientFuelSurchargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFuelSurchargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFuelSurchargeWindowStore(
    var items: MutableMap<String, LenientFuelSurchargeWindow>,
    var order: MutableList<String>,
) : LenientFuelSurchargeWindowStore {
    override fun find(code: String): LenientFuelSurchargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFuelSurchargeWindow) {
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

    override fun list(): MutableList<LenientFuelSurchargeWindow> {
        val out = mutableListOf<LenientFuelSurchargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFuelSurchargeWindowStore(): LenientFuelSurchargeWindowStore {
    return MemoryLenientFuelSurchargeWindowStore(
        mutableMapOf<String, LenientFuelSurchargeWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFuelSurchargeWindowStore(
    var inner: LenientFuelSurchargeWindowStore,
    var cache: TtlCache<LenientFuelSurchargeWindow>,
    var hits: Int,
    var misses: Int,
) : LenientFuelSurchargeWindowStore {
    override fun find(code: String): LenientFuelSurchargeWindow? {
        val hit = (cache.get(code) as LenientFuelSurchargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFuelSurchargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFuelSurchargeWindow> = inner.list()
}

fun newCachedLenientFuelSurchargeWindowStore(inner: LenientFuelSurchargeWindowStore, ttl: Int, clock: Clock): LenientFuelSurchargeWindowStore {
    return CachedLenientFuelSurchargeWindowStore(
        inner = inner,
        cache = newTtlCache<LenientFuelSurchargeWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient fuel surcharge windows. Empty fields match all. */
data class LenientFuelSurchargeWindowQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFuelSurchargeWindow): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFuelSurchargeWindow>): MutableList<LenientFuelSurchargeWindow> {
        val matched = filter(items) { candidate: LenientFuelSurchargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFuelSurchargeWindows(store: LenientFuelSurchargeWindowStore, query: LenientFuelSurchargeWindowQuery): MutableList<LenientFuelSurchargeWindow> {
    return query.apply(store.list())
}
