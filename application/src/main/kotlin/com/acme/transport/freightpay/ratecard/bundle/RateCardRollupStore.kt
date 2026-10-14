package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card rollups by code. */
interface RateCardRollupStore {
    fun find(code: String): RateCardRollup?
    fun save(item: RateCardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardRollupStore(
    var items: MutableMap<String, RateCardRollup>,
    var order: MutableList<String>,
) : RateCardRollupStore {
    override fun find(code: String): RateCardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardRollup) {
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

    override fun list(): MutableList<RateCardRollup> {
        val out = mutableListOf<RateCardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardRollupStore(): RateCardRollupStore {
    return MemoryRateCardRollupStore(mutableMapOf<String, RateCardRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardRollupStore(
    var inner: RateCardRollupStore,
    var cache: TtlCache<RateCardRollup>,
    var hits: Int,
    var misses: Int,
) : RateCardRollupStore {
    override fun find(code: String): RateCardRollup? {
        val hit = (cache.get(code) as RateCardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardRollup> = inner.list()
}

fun newCachedRateCardRollupStore(inner: RateCardRollupStore, ttl: Int, clock: Clock): RateCardRollupStore {
    return CachedRateCardRollupStore(
        inner = inner,
        cache = newTtlCache<RateCardRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card rollups. Empty fields match all. */
data class RateCardRollupQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardRollup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardRollup>): MutableList<RateCardRollup> {
        val matched = filter(items) { candidate: RateCardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardRollups(store: RateCardRollupStore, query: RateCardRollupQuery): MutableList<RateCardRollup> {
    return query.apply(store.list())
}
