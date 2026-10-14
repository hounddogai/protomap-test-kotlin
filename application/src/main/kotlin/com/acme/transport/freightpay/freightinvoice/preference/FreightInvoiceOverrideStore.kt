package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice overrides by code. */
interface FreightInvoiceOverrideStore {
    fun find(code: String): FreightInvoiceOverride?
    fun save(item: FreightInvoiceOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceOverrideStore(
    var items: MutableMap<String, FreightInvoiceOverride>,
    var order: MutableList<String>,
) : FreightInvoiceOverrideStore {
    override fun find(code: String): FreightInvoiceOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceOverride) {
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

    override fun list(): MutableList<FreightInvoiceOverride> {
        val out = mutableListOf<FreightInvoiceOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceOverrideStore(): FreightInvoiceOverrideStore {
    return MemoryFreightInvoiceOverrideStore(
        mutableMapOf<String, FreightInvoiceOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceOverrideStore(
    var inner: FreightInvoiceOverrideStore,
    var cache: TtlCache<FreightInvoiceOverride>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceOverrideStore {
    override fun find(code: String): FreightInvoiceOverride? {
        val hit = (cache.get(code) as FreightInvoiceOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceOverride> = inner.list()
}

fun newCachedFreightInvoiceOverrideStore(inner: FreightInvoiceOverrideStore, ttl: Int, clock: Clock): FreightInvoiceOverrideStore {
    return CachedFreightInvoiceOverrideStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice overrides. Empty fields match all. */
data class FreightInvoiceOverrideQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceOverride): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceOverride>): MutableList<FreightInvoiceOverride> {
        val matched = filter(items) { candidate: FreightInvoiceOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceOverrides(store: FreightInvoiceOverrideStore, query: FreightInvoiceOverrideQuery): MutableList<FreightInvoiceOverride> {
    return query.apply(store.list())
}
