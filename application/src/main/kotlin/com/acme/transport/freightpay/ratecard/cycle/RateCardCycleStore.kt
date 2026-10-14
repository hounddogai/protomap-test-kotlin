package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card cycles by code. */
interface RateCardCycleStore {
    fun find(code: String): RateCardCycle?
    fun save(item: RateCardCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardCycleStore(
    var items: MutableMap<String, RateCardCycle>,
    var order: MutableList<String>,
) : RateCardCycleStore {
    override fun find(code: String): RateCardCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardCycle) {
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

    override fun list(): MutableList<RateCardCycle> {
        val out = mutableListOf<RateCardCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardCycleStore(): RateCardCycleStore {
    return MemoryRateCardCycleStore(mutableMapOf<String, RateCardCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardCycleStore(
    var inner: RateCardCycleStore,
    var cache: TtlCache<RateCardCycle>,
    var hits: Int,
    var misses: Int,
) : RateCardCycleStore {
    override fun find(code: String): RateCardCycle? {
        val hit = (cache.get(code) as RateCardCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardCycle> = inner.list()
}

fun newCachedRateCardCycleStore(inner: RateCardCycleStore, ttl: Int, clock: Clock): RateCardCycleStore {
    return CachedRateCardCycleStore(
        inner = inner,
        cache = newTtlCache<RateCardCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card cycles. Empty fields match all. */
data class RateCardCycleQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardCycle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardCycle>): MutableList<RateCardCycle> {
        val matched = filter(items) { candidate: RateCardCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardCycles(store: RateCardCycleStore, query: RateCardCycleQuery): MutableList<RateCardCycle> {
    return query.apply(store.list())
}
