package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict freight invoice scores by code. */
interface StrictFreightInvoiceScoreStore {
    fun find(code: String): StrictFreightInvoiceScore?
    fun save(item: StrictFreightInvoiceScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictFreightInvoiceScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictFreightInvoiceScoreStore(
    var items: MutableMap<String, StrictFreightInvoiceScore>,
    var order: MutableList<String>,
) : StrictFreightInvoiceScoreStore {
    override fun find(code: String): StrictFreightInvoiceScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictFreightInvoiceScore) {
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

    override fun list(): MutableList<StrictFreightInvoiceScore> {
        val out = mutableListOf<StrictFreightInvoiceScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictFreightInvoiceScoreStore(): StrictFreightInvoiceScoreStore {
    return MemoryStrictFreightInvoiceScoreStore(
        mutableMapOf<String, StrictFreightInvoiceScore>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictFreightInvoiceScoreStore(
    var inner: StrictFreightInvoiceScoreStore,
    var cache: TtlCache<StrictFreightInvoiceScore>,
    var hits: Int,
    var misses: Int,
) : StrictFreightInvoiceScoreStore {
    override fun find(code: String): StrictFreightInvoiceScore? {
        val hit = (cache.get(code) as StrictFreightInvoiceScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictFreightInvoiceScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictFreightInvoiceScore> = inner.list()
}

fun newCachedStrictFreightInvoiceScoreStore(inner: StrictFreightInvoiceScoreStore, ttl: Int, clock: Clock): StrictFreightInvoiceScoreStore {
    return CachedStrictFreightInvoiceScoreStore(
        inner = inner,
        cache = newTtlCache<StrictFreightInvoiceScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict freight invoice scores. Empty fields match all. */
data class StrictFreightInvoiceScoreQuery(
    var region: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictFreightInvoiceScore): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictFreightInvoiceScore>): MutableList<StrictFreightInvoiceScore> {
        val matched = filter(items) { candidate: StrictFreightInvoiceScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictFreightInvoiceScores(store: StrictFreightInvoiceScoreStore, query: StrictFreightInvoiceScoreQuery): MutableList<StrictFreightInvoiceScore> {
    return query.apply(store.list())
}
