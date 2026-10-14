package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default rate card cycles by code. */
interface DefaultRateCardCycleStore {
    fun find(code: String): DefaultRateCardCycle?
    fun save(item: DefaultRateCardCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRateCardCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRateCardCycleStore(
    var items: MutableMap<String, DefaultRateCardCycle>,
    var order: MutableList<String>,
) : DefaultRateCardCycleStore {
    override fun find(code: String): DefaultRateCardCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRateCardCycle) {
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

    override fun list(): MutableList<DefaultRateCardCycle> {
        val out = mutableListOf<DefaultRateCardCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRateCardCycleStore(): DefaultRateCardCycleStore {
    return MemoryDefaultRateCardCycleStore(
        mutableMapOf<String, DefaultRateCardCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRateCardCycleStore(
    var inner: DefaultRateCardCycleStore,
    var cache: TtlCache<DefaultRateCardCycle>,
    var hits: Int,
    var misses: Int,
) : DefaultRateCardCycleStore {
    override fun find(code: String): DefaultRateCardCycle? {
        val hit = (cache.get(code) as DefaultRateCardCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRateCardCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRateCardCycle> = inner.list()
}

fun newCachedDefaultRateCardCycleStore(inner: DefaultRateCardCycleStore, ttl: Int, clock: Clock): DefaultRateCardCycleStore {
    return CachedDefaultRateCardCycleStore(
        inner = inner,
        cache = newTtlCache<DefaultRateCardCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default rate card cycles. Empty fields match all. */
data class DefaultRateCardCycleQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRateCardCycle): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRateCardCycle>): MutableList<DefaultRateCardCycle> {
        val matched = filter(items) { candidate: DefaultRateCardCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRateCardCycles(store: DefaultRateCardCycleStore, query: DefaultRateCardCycleQuery): MutableList<DefaultRateCardCycle> {
    return query.apply(store.list())
}
