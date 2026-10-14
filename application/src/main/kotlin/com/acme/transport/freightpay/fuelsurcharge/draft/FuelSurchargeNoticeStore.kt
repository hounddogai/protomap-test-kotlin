package com.acme.transport.freightpay.fuelsurcharge.draft

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge notices by code. */
interface FuelSurchargeNoticeStore {
    fun find(code: String): FuelSurchargeNotice?
    fun save(item: FuelSurchargeNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeNoticeStore(
    var items: MutableMap<String, FuelSurchargeNotice>,
    var order: MutableList<String>,
) : FuelSurchargeNoticeStore {
    override fun find(code: String): FuelSurchargeNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeNotice) {
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

    override fun list(): MutableList<FuelSurchargeNotice> {
        val out = mutableListOf<FuelSurchargeNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeNoticeStore(): FuelSurchargeNoticeStore {
    return MemoryFuelSurchargeNoticeStore(mutableMapOf<String, FuelSurchargeNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeNoticeStore(
    var inner: FuelSurchargeNoticeStore,
    var cache: TtlCache<FuelSurchargeNotice>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeNoticeStore {
    override fun find(code: String): FuelSurchargeNotice? {
        val hit = (cache.get(code) as FuelSurchargeNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeNotice> = inner.list()
}

fun newCachedFuelSurchargeNoticeStore(inner: FuelSurchargeNoticeStore, ttl: Int, clock: Clock): FuelSurchargeNoticeStore {
    return CachedFuelSurchargeNoticeStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeNotice>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge notices. Empty fields match all. */
data class FuelSurchargeNoticeQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeNotice): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeNotice>): MutableList<FuelSurchargeNotice> {
        val matched = filter(items) { candidate: FuelSurchargeNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeNotices(store: FuelSurchargeNoticeStore, query: FuelSurchargeNoticeQuery): MutableList<FuelSurchargeNotice> {
    return query.apply(store.list())
}
