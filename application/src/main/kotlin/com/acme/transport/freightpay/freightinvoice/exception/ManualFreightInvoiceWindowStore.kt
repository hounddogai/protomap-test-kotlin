package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual freight invoice windows by code. */
interface ManualFreightInvoiceWindowStore {
    fun find(code: String): ManualFreightInvoiceWindow?
    fun save(item: ManualFreightInvoiceWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFreightInvoiceWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFreightInvoiceWindowStore(
    var items: MutableMap<String, ManualFreightInvoiceWindow>,
    var order: MutableList<String>,
) : ManualFreightInvoiceWindowStore {
    override fun find(code: String): ManualFreightInvoiceWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFreightInvoiceWindow) {
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

    override fun list(): MutableList<ManualFreightInvoiceWindow> {
        val out = mutableListOf<ManualFreightInvoiceWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFreightInvoiceWindowStore(): ManualFreightInvoiceWindowStore {
    return MemoryManualFreightInvoiceWindowStore(
        mutableMapOf<String, ManualFreightInvoiceWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFreightInvoiceWindowStore(
    var inner: ManualFreightInvoiceWindowStore,
    var cache: TtlCache<ManualFreightInvoiceWindow>,
    var hits: Int,
    var misses: Int,
) : ManualFreightInvoiceWindowStore {
    override fun find(code: String): ManualFreightInvoiceWindow? {
        val hit = (cache.get(code) as ManualFreightInvoiceWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFreightInvoiceWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFreightInvoiceWindow> = inner.list()
}

fun newCachedManualFreightInvoiceWindowStore(inner: ManualFreightInvoiceWindowStore, ttl: Int, clock: Clock): ManualFreightInvoiceWindowStore {
    return CachedManualFreightInvoiceWindowStore(
        inner = inner,
        cache = newTtlCache<ManualFreightInvoiceWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual freight invoice windows. Empty fields match all. */
data class ManualFreightInvoiceWindowQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFreightInvoiceWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualFreightInvoiceWindow>): MutableList<ManualFreightInvoiceWindow> {
        val matched = filter(items) { candidate: ManualFreightInvoiceWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFreightInvoiceWindows(store: ManualFreightInvoiceWindowStore, query: ManualFreightInvoiceWindowQuery): MutableList<ManualFreightInvoiceWindow> {
    return query.apply(store.list())
}
