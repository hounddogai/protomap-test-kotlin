package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy fuel surcharge windows by code. */
interface LegacyFuelSurchargeWindowStore {
    fun find(code: String): LegacyFuelSurchargeWindow?
    fun save(item: LegacyFuelSurchargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyFuelSurchargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyFuelSurchargeWindowStore(
    var items: MutableMap<String, LegacyFuelSurchargeWindow>,
    var order: MutableList<String>,
) : LegacyFuelSurchargeWindowStore {
    override fun find(code: String): LegacyFuelSurchargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyFuelSurchargeWindow) {
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

    override fun list(): MutableList<LegacyFuelSurchargeWindow> {
        val out = mutableListOf<LegacyFuelSurchargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyFuelSurchargeWindowStore(): LegacyFuelSurchargeWindowStore {
    return MemoryLegacyFuelSurchargeWindowStore(
        mutableMapOf<String, LegacyFuelSurchargeWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyFuelSurchargeWindowStore(
    var inner: LegacyFuelSurchargeWindowStore,
    var cache: TtlCache<LegacyFuelSurchargeWindow>,
    var hits: Int,
    var misses: Int,
) : LegacyFuelSurchargeWindowStore {
    override fun find(code: String): LegacyFuelSurchargeWindow? {
        val hit = (cache.get(code) as LegacyFuelSurchargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyFuelSurchargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyFuelSurchargeWindow> = inner.list()
}

fun newCachedLegacyFuelSurchargeWindowStore(inner: LegacyFuelSurchargeWindowStore, ttl: Int, clock: Clock): LegacyFuelSurchargeWindowStore {
    return CachedLegacyFuelSurchargeWindowStore(
        inner = inner,
        cache = newTtlCache<LegacyFuelSurchargeWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy fuel surcharge windows. Empty fields match all. */
data class LegacyFuelSurchargeWindowQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyFuelSurchargeWindow): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyFuelSurchargeWindow>): MutableList<LegacyFuelSurchargeWindow> {
        val matched = filter(items) { candidate: LegacyFuelSurchargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyFuelSurchargeWindows(store: LegacyFuelSurchargeWindowStore, query: LegacyFuelSurchargeWindowQuery): MutableList<LegacyFuelSurchargeWindow> {
    return query.apply(store.list())
}
