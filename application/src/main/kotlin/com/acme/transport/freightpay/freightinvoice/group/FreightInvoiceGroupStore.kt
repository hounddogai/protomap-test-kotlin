package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice groups by code. */
interface FreightInvoiceGroupStore {
    fun find(code: String): FreightInvoiceGroup?
    fun save(item: FreightInvoiceGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceGroupStore(
    var items: MutableMap<String, FreightInvoiceGroup>,
    var order: MutableList<String>,
) : FreightInvoiceGroupStore {
    override fun find(code: String): FreightInvoiceGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceGroup) {
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

    override fun list(): MutableList<FreightInvoiceGroup> {
        val out = mutableListOf<FreightInvoiceGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceGroupStore(): FreightInvoiceGroupStore {
    return MemoryFreightInvoiceGroupStore(mutableMapOf<String, FreightInvoiceGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceGroupStore(
    var inner: FreightInvoiceGroupStore,
    var cache: TtlCache<FreightInvoiceGroup>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceGroupStore {
    override fun find(code: String): FreightInvoiceGroup? {
        val hit = (cache.get(code) as FreightInvoiceGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceGroup> = inner.list()
}

fun newCachedFreightInvoiceGroupStore(inner: FreightInvoiceGroupStore, ttl: Int, clock: Clock): FreightInvoiceGroupStore {
    return CachedFreightInvoiceGroupStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice groups. Empty fields match all. */
data class FreightInvoiceGroupQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceGroup>): MutableList<FreightInvoiceGroup> {
        val matched = filter(items) { candidate: FreightInvoiceGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceGroups(store: FreightInvoiceGroupStore, query: FreightInvoiceGroupQuery): MutableList<FreightInvoiceGroup> {
    return query.apply(store.list())
}
