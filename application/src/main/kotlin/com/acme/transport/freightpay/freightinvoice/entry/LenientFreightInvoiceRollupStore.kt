package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient freight invoice rollups by code. */
interface LenientFreightInvoiceRollupStore {
    fun find(code: String): LenientFreightInvoiceRollup?
    fun save(item: LenientFreightInvoiceRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFreightInvoiceRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFreightInvoiceRollupStore(
    var items: MutableMap<String, LenientFreightInvoiceRollup>,
    var order: MutableList<String>,
) : LenientFreightInvoiceRollupStore {
    override fun find(code: String): LenientFreightInvoiceRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFreightInvoiceRollup) {
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

    override fun list(): MutableList<LenientFreightInvoiceRollup> {
        val out = mutableListOf<LenientFreightInvoiceRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFreightInvoiceRollupStore(): LenientFreightInvoiceRollupStore {
    return MemoryLenientFreightInvoiceRollupStore(
        mutableMapOf<String, LenientFreightInvoiceRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFreightInvoiceRollupStore(
    var inner: LenientFreightInvoiceRollupStore,
    var cache: TtlCache<LenientFreightInvoiceRollup>,
    var hits: Int,
    var misses: Int,
) : LenientFreightInvoiceRollupStore {
    override fun find(code: String): LenientFreightInvoiceRollup? {
        val hit = (cache.get(code) as LenientFreightInvoiceRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFreightInvoiceRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFreightInvoiceRollup> = inner.list()
}

fun newCachedLenientFreightInvoiceRollupStore(inner: LenientFreightInvoiceRollupStore, ttl: Int, clock: Clock): LenientFreightInvoiceRollupStore {
    return CachedLenientFreightInvoiceRollupStore(
        inner = inner,
        cache = newTtlCache<LenientFreightInvoiceRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient freight invoice rollups. Empty fields match all. */
data class LenientFreightInvoiceRollupQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFreightInvoiceRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFreightInvoiceRollup>): MutableList<LenientFreightInvoiceRollup> {
        val matched = filter(items) { candidate: LenientFreightInvoiceRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFreightInvoiceRollups(store: LenientFreightInvoiceRollupStore, query: LenientFreightInvoiceRollupQuery): MutableList<LenientFreightInvoiceRollup> {
    return query.apply(store.list())
}
