package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn notices by code. */
interface FuelBurnNoticeStore {
    fun find(code: String): FuelBurnNotice?
    fun save(item: FuelBurnNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnNoticeStore(
    var items: MutableMap<String, FuelBurnNotice>,
    var order: MutableList<String>,
) : FuelBurnNoticeStore {
    override fun find(code: String): FuelBurnNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnNotice) {
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

    override fun list(): MutableList<FuelBurnNotice> {
        val out = mutableListOf<FuelBurnNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnNoticeStore(): FuelBurnNoticeStore {
    return MemoryFuelBurnNoticeStore(mutableMapOf<String, FuelBurnNotice>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnNoticeStore(
    var inner: FuelBurnNoticeStore,
    var cache: TtlCache<FuelBurnNotice>,
    var hits: Int,
    var misses: Int,
) : FuelBurnNoticeStore {
    override fun find(code: String): FuelBurnNotice? {
        val hit = (cache.get(code) as FuelBurnNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnNotice> = inner.list()
}

fun newCachedFuelBurnNoticeStore(inner: FuelBurnNoticeStore, ttl: Int, clock: Clock): FuelBurnNoticeStore {
    return CachedFuelBurnNoticeStore(
        inner = inner,
        cache = newTtlCache<FuelBurnNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn notices. Empty fields match all. */
data class FuelBurnNoticeQuery(
    var locale: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnNotice): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnNotice>): MutableList<FuelBurnNotice> {
        val matched = filter(items) { candidate: FuelBurnNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnNotices(store: FuelBurnNoticeStore, query: FuelBurnNoticeQuery): MutableList<FuelBurnNotice> {
    return query.apply(store.list())
}
