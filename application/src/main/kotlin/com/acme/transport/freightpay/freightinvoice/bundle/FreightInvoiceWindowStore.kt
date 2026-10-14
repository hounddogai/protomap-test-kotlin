package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice windows by code. */
interface FreightInvoiceWindowStore {
    fun find(code: String): FreightInvoiceWindow?
    fun save(item: FreightInvoiceWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceWindowStore(
    var items: MutableMap<String, FreightInvoiceWindow>,
    var order: MutableList<String>,
) : FreightInvoiceWindowStore {
    override fun find(code: String): FreightInvoiceWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceWindow) {
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

    override fun list(): MutableList<FreightInvoiceWindow> {
        val out = mutableListOf<FreightInvoiceWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceWindowStore(): FreightInvoiceWindowStore {
    return MemoryFreightInvoiceWindowStore(
        mutableMapOf<String, FreightInvoiceWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceWindowStore(
    var inner: FreightInvoiceWindowStore,
    var cache: TtlCache<FreightInvoiceWindow>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceWindowStore {
    override fun find(code: String): FreightInvoiceWindow? {
        val hit = (cache.get(code) as FreightInvoiceWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceWindow> = inner.list()
}

fun newCachedFreightInvoiceWindowStore(inner: FreightInvoiceWindowStore, ttl: Int, clock: Clock): FreightInvoiceWindowStore {
    return CachedFreightInvoiceWindowStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice windows. Empty fields match all. */
data class FreightInvoiceWindowQuery(
    var channel: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceWindow): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceWindow>): MutableList<FreightInvoiceWindow> {
        val matched = filter(items) { candidate: FreightInvoiceWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceWindows(store: FreightInvoiceWindowStore, query: FreightInvoiceWindowQuery): MutableList<FreightInvoiceWindow> {
    return query.apply(store.list())
}
