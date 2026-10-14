package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict freight invoice overrides by code. */
interface StrictFreightInvoiceOverrideStore {
    fun find(code: String): StrictFreightInvoiceOverride?
    fun save(item: StrictFreightInvoiceOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictFreightInvoiceOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictFreightInvoiceOverrideStore(
    var items: MutableMap<String, StrictFreightInvoiceOverride>,
    var order: MutableList<String>,
) : StrictFreightInvoiceOverrideStore {
    override fun find(code: String): StrictFreightInvoiceOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictFreightInvoiceOverride) {
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

    override fun list(): MutableList<StrictFreightInvoiceOverride> {
        val out = mutableListOf<StrictFreightInvoiceOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictFreightInvoiceOverrideStore(): StrictFreightInvoiceOverrideStore {
    return MemoryStrictFreightInvoiceOverrideStore(
        mutableMapOf<String, StrictFreightInvoiceOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictFreightInvoiceOverrideStore(
    var inner: StrictFreightInvoiceOverrideStore,
    var cache: TtlCache<StrictFreightInvoiceOverride>,
    var hits: Int,
    var misses: Int,
) : StrictFreightInvoiceOverrideStore {
    override fun find(code: String): StrictFreightInvoiceOverride? {
        val hit = (cache.get(code) as StrictFreightInvoiceOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictFreightInvoiceOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictFreightInvoiceOverride> = inner.list()
}

fun newCachedStrictFreightInvoiceOverrideStore(inner: StrictFreightInvoiceOverrideStore, ttl: Int, clock: Clock): StrictFreightInvoiceOverrideStore {
    return CachedStrictFreightInvoiceOverrideStore(
        inner = inner,
        cache = newTtlCache<StrictFreightInvoiceOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict freight invoice overrides. Empty fields match all. */
data class StrictFreightInvoiceOverrideQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictFreightInvoiceOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictFreightInvoiceOverride>): MutableList<StrictFreightInvoiceOverride> {
        val matched = filter(items) { candidate: StrictFreightInvoiceOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictFreightInvoiceOverrides(store: StrictFreightInvoiceOverrideStore, query: StrictFreightInvoiceOverrideQuery): MutableList<StrictFreightInvoiceOverride> {
    return query.apply(store.list())
}
