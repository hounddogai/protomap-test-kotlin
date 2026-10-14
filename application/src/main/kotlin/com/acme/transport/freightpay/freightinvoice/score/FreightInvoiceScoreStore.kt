package com.acme.transport.freightpay.freightinvoice.score

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice scores by code. */
interface FreightInvoiceScoreStore {
    fun find(code: String): FreightInvoiceScore?
    fun save(item: FreightInvoiceScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceScoreStore(
    var items: MutableMap<String, FreightInvoiceScore>,
    var order: MutableList<String>,
) : FreightInvoiceScoreStore {
    override fun find(code: String): FreightInvoiceScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceScore) {
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

    override fun list(): MutableList<FreightInvoiceScore> {
        val out = mutableListOf<FreightInvoiceScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceScoreStore(): FreightInvoiceScoreStore {
    return MemoryFreightInvoiceScoreStore(mutableMapOf<String, FreightInvoiceScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceScoreStore(
    var inner: FreightInvoiceScoreStore,
    var cache: TtlCache<FreightInvoiceScore>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceScoreStore {
    override fun find(code: String): FreightInvoiceScore? {
        val hit = (cache.get(code) as FreightInvoiceScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceScore> = inner.list()
}

fun newCachedFreightInvoiceScoreStore(inner: FreightInvoiceScoreStore, ttl: Int, clock: Clock): FreightInvoiceScoreStore {
    return CachedFreightInvoiceScoreStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceScore>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice scores. Empty fields match all. */
data class FreightInvoiceScoreQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceScore): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceScore>): MutableList<FreightInvoiceScore> {
        val matched = filter(items) { candidate: FreightInvoiceScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceScores(store: FreightInvoiceScoreStore, query: FreightInvoiceScoreQuery): MutableList<FreightInvoiceScore> {
    return query.apply(store.list())
}
