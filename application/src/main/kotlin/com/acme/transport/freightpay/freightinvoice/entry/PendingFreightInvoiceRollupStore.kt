package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending freight invoice rollups by code. */
interface PendingFreightInvoiceRollupStore {
    fun find(code: String): PendingFreightInvoiceRollup?
    fun save(item: PendingFreightInvoiceRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFreightInvoiceRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFreightInvoiceRollupStore(
    var items: MutableMap<String, PendingFreightInvoiceRollup>,
    var order: MutableList<String>,
) : PendingFreightInvoiceRollupStore {
    override fun find(code: String): PendingFreightInvoiceRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFreightInvoiceRollup) {
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

    override fun list(): MutableList<PendingFreightInvoiceRollup> {
        val out = mutableListOf<PendingFreightInvoiceRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFreightInvoiceRollupStore(): PendingFreightInvoiceRollupStore {
    return MemoryPendingFreightInvoiceRollupStore(
        mutableMapOf<String, PendingFreightInvoiceRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFreightInvoiceRollupStore(
    var inner: PendingFreightInvoiceRollupStore,
    var cache: TtlCache<PendingFreightInvoiceRollup>,
    var hits: Int,
    var misses: Int,
) : PendingFreightInvoiceRollupStore {
    override fun find(code: String): PendingFreightInvoiceRollup? {
        val hit = (cache.get(code) as PendingFreightInvoiceRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFreightInvoiceRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFreightInvoiceRollup> = inner.list()
}

fun newCachedPendingFreightInvoiceRollupStore(inner: PendingFreightInvoiceRollupStore, ttl: Int, clock: Clock): PendingFreightInvoiceRollupStore {
    return CachedPendingFreightInvoiceRollupStore(
        inner = inner,
        cache = newTtlCache<PendingFreightInvoiceRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending freight invoice rollups. Empty fields match all. */
data class PendingFreightInvoiceRollupQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFreightInvoiceRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingFreightInvoiceRollup>): MutableList<PendingFreightInvoiceRollup> {
        val matched = filter(items) { candidate: PendingFreightInvoiceRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFreightInvoiceRollups(store: PendingFreightInvoiceRollupStore, query: PendingFreightInvoiceRollupQuery): MutableList<PendingFreightInvoiceRollup> {
    return query.apply(store.list())
}
