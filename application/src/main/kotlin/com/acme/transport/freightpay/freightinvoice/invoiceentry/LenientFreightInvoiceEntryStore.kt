package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient freight invoice entries by code. */
interface LenientFreightInvoiceEntryStore {
    fun find(code: String): LenientFreightInvoiceEntry?
    fun save(item: LenientFreightInvoiceEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFreightInvoiceEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFreightInvoiceEntryStore(
    var items: MutableMap<String, LenientFreightInvoiceEntry>,
    var order: MutableList<String>,
) : LenientFreightInvoiceEntryStore {
    override fun find(code: String): LenientFreightInvoiceEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFreightInvoiceEntry) {
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

    override fun list(): MutableList<LenientFreightInvoiceEntry> {
        val out = mutableListOf<LenientFreightInvoiceEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFreightInvoiceEntryStore(): LenientFreightInvoiceEntryStore {
    return MemoryLenientFreightInvoiceEntryStore(
        mutableMapOf<String, LenientFreightInvoiceEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFreightInvoiceEntryStore(
    var inner: LenientFreightInvoiceEntryStore,
    var cache: TtlCache<LenientFreightInvoiceEntry>,
    var hits: Int,
    var misses: Int,
) : LenientFreightInvoiceEntryStore {
    override fun find(code: String): LenientFreightInvoiceEntry? {
        val hit = (cache.get(code) as LenientFreightInvoiceEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFreightInvoiceEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFreightInvoiceEntry> = inner.list()
}

fun newCachedLenientFreightInvoiceEntryStore(inner: LenientFreightInvoiceEntryStore, ttl: Int, clock: Clock): LenientFreightInvoiceEntryStore {
    return CachedLenientFreightInvoiceEntryStore(
        inner = inner,
        cache = newTtlCache<LenientFreightInvoiceEntry>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient freight invoice entries. Empty fields match all. */
data class LenientFreightInvoiceEntryQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFreightInvoiceEntry): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFreightInvoiceEntry>): MutableList<LenientFreightInvoiceEntry> {
        val matched = filter(items) { candidate: LenientFreightInvoiceEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFreightInvoiceEntries(store: LenientFreightInvoiceEntryStore, query: LenientFreightInvoiceEntryQuery): MutableList<LenientFreightInvoiceEntry> {
    return query.apply(store.list())
}
