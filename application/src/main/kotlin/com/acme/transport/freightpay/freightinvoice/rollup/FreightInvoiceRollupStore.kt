package com.acme.transport.freightpay.freightinvoice.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice rollups by code. */
interface FreightInvoiceRollupStore {
    fun find(code: String): FreightInvoiceRollup?
    fun save(item: FreightInvoiceRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceRollupStore(
    var items: MutableMap<String, FreightInvoiceRollup>,
    var order: MutableList<String>,
) : FreightInvoiceRollupStore {
    override fun find(code: String): FreightInvoiceRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceRollup) {
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

    override fun list(): MutableList<FreightInvoiceRollup> {
        val out = mutableListOf<FreightInvoiceRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceRollupStore(): FreightInvoiceRollupStore {
    return MemoryFreightInvoiceRollupStore(
        mutableMapOf<String, FreightInvoiceRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceRollupStore(
    var inner: FreightInvoiceRollupStore,
    var cache: TtlCache<FreightInvoiceRollup>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceRollupStore {
    override fun find(code: String): FreightInvoiceRollup? {
        val hit = (cache.get(code) as FreightInvoiceRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceRollup> = inner.list()
}

fun newCachedFreightInvoiceRollupStore(inner: FreightInvoiceRollupStore, ttl: Int, clock: Clock): FreightInvoiceRollupStore {
    return CachedFreightInvoiceRollupStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice rollups. Empty fields match all. */
data class FreightInvoiceRollupQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceRollup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceRollup>): MutableList<FreightInvoiceRollup> {
        val matched = filter(items) { candidate: FreightInvoiceRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceRollups(store: FreightInvoiceRollupStore, query: FreightInvoiceRollupQuery): MutableList<FreightInvoiceRollup> {
    return query.apply(store.list())
}
