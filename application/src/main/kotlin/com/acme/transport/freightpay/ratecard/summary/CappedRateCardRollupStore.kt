package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped rate card rollups by code. */
interface CappedRateCardRollupStore {
    fun find(code: String): CappedRateCardRollup?
    fun save(item: CappedRateCardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRateCardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRateCardRollupStore(
    var items: MutableMap<String, CappedRateCardRollup>,
    var order: MutableList<String>,
) : CappedRateCardRollupStore {
    override fun find(code: String): CappedRateCardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRateCardRollup) {
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

    override fun list(): MutableList<CappedRateCardRollup> {
        val out = mutableListOf<CappedRateCardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRateCardRollupStore(): CappedRateCardRollupStore {
    return MemoryCappedRateCardRollupStore(
        mutableMapOf<String, CappedRateCardRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRateCardRollupStore(
    var inner: CappedRateCardRollupStore,
    var cache: TtlCache<CappedRateCardRollup>,
    var hits: Int,
    var misses: Int,
) : CappedRateCardRollupStore {
    override fun find(code: String): CappedRateCardRollup? {
        val hit = (cache.get(code) as CappedRateCardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRateCardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRateCardRollup> = inner.list()
}

fun newCachedCappedRateCardRollupStore(inner: CappedRateCardRollupStore, ttl: Int, clock: Clock): CappedRateCardRollupStore {
    return CachedCappedRateCardRollupStore(
        inner = inner,
        cache = newTtlCache<CappedRateCardRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped rate card rollups. Empty fields match all. */
data class CappedRateCardRollupQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRateCardRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRateCardRollup>): MutableList<CappedRateCardRollup> {
        val matched = filter(items) { candidate: CappedRateCardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRateCardRollups(store: CappedRateCardRollupStore, query: CappedRateCardRollupQuery): MutableList<CappedRateCardRollup> {
    return query.apply(store.list())
}
