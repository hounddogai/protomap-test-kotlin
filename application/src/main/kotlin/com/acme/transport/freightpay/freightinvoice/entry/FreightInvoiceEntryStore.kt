package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice entries by code. */
interface FreightInvoiceEntryStore {
    fun find(code: String): FreightInvoiceEntry?
    fun save(item: FreightInvoiceEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceEntryStore(
    var items: MutableMap<String, FreightInvoiceEntry>,
    var order: MutableList<String>,
) : FreightInvoiceEntryStore {
    override fun find(code: String): FreightInvoiceEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceEntry) {
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

    override fun list(): MutableList<FreightInvoiceEntry> {
        val out = mutableListOf<FreightInvoiceEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceEntryStore(): FreightInvoiceEntryStore {
    return MemoryFreightInvoiceEntryStore(mutableMapOf<String, FreightInvoiceEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceEntryStore(
    var inner: FreightInvoiceEntryStore,
    var cache: TtlCache<FreightInvoiceEntry>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceEntryStore {
    override fun find(code: String): FreightInvoiceEntry? {
        val hit = (cache.get(code) as FreightInvoiceEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceEntry> = inner.list()
}

fun newCachedFreightInvoiceEntryStore(inner: FreightInvoiceEntryStore, ttl: Int, clock: Clock): FreightInvoiceEntryStore {
    return CachedFreightInvoiceEntryStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceEntry>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice entries. Empty fields match all. */
data class FreightInvoiceEntryQuery(
    var channel: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceEntry): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceEntry>): MutableList<FreightInvoiceEntry> {
        val matched = filter(items) { candidate: FreightInvoiceEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceEntries(store: FreightInvoiceEntryStore, query: FreightInvoiceEntryQuery): MutableList<FreightInvoiceEntry> {
    return query.apply(store.list())
}
