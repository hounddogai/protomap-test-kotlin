package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled rate card rollups by code. */
interface ScheduledRateCardRollupStore {
    fun find(code: String): ScheduledRateCardRollup?
    fun save(item: ScheduledRateCardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRateCardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRateCardRollupStore(
    var items: MutableMap<String, ScheduledRateCardRollup>,
    var order: MutableList<String>,
) : ScheduledRateCardRollupStore {
    override fun find(code: String): ScheduledRateCardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRateCardRollup) {
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

    override fun list(): MutableList<ScheduledRateCardRollup> {
        val out = mutableListOf<ScheduledRateCardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRateCardRollupStore(): ScheduledRateCardRollupStore {
    return MemoryScheduledRateCardRollupStore(
        mutableMapOf<String, ScheduledRateCardRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRateCardRollupStore(
    var inner: ScheduledRateCardRollupStore,
    var cache: TtlCache<ScheduledRateCardRollup>,
    var hits: Int,
    var misses: Int,
) : ScheduledRateCardRollupStore {
    override fun find(code: String): ScheduledRateCardRollup? {
        val hit = (cache.get(code) as ScheduledRateCardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRateCardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRateCardRollup> = inner.list()
}

fun newCachedScheduledRateCardRollupStore(inner: ScheduledRateCardRollupStore, ttl: Int, clock: Clock): ScheduledRateCardRollupStore {
    return CachedScheduledRateCardRollupStore(
        inner = inner,
        cache = newTtlCache<ScheduledRateCardRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled rate card rollups. Empty fields match all. */
data class ScheduledRateCardRollupQuery(
    var region: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRateCardRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRateCardRollup>): MutableList<ScheduledRateCardRollup> {
        val matched = filter(items) { candidate: ScheduledRateCardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRateCardRollups(store: ScheduledRateCardRollupStore, query: ScheduledRateCardRollupQuery): MutableList<ScheduledRateCardRollup> {
    return query.apply(store.list())
}
