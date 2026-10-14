package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice tiers by code. */
interface FreightInvoiceTierStore {
    fun find(code: String): FreightInvoiceTier?
    fun save(item: FreightInvoiceTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceTierStore(
    var items: MutableMap<String, FreightInvoiceTier>,
    var order: MutableList<String>,
) : FreightInvoiceTierStore {
    override fun find(code: String): FreightInvoiceTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceTier) {
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

    override fun list(): MutableList<FreightInvoiceTier> {
        val out = mutableListOf<FreightInvoiceTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceTierStore(): FreightInvoiceTierStore {
    return MemoryFreightInvoiceTierStore(mutableMapOf<String, FreightInvoiceTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceTierStore(
    var inner: FreightInvoiceTierStore,
    var cache: TtlCache<FreightInvoiceTier>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceTierStore {
    override fun find(code: String): FreightInvoiceTier? {
        val hit = (cache.get(code) as FreightInvoiceTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceTier> = inner.list()
}

fun newCachedFreightInvoiceTierStore(inner: FreightInvoiceTierStore, ttl: Int, clock: Clock): FreightInvoiceTierStore {
    return CachedFreightInvoiceTierStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceTier>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice tiers. Empty fields match all. */
data class FreightInvoiceTierQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceTier>): MutableList<FreightInvoiceTier> {
        val matched = filter(items) { candidate: FreightInvoiceTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceTiers(store: FreightInvoiceTierStore, query: FreightInvoiceTierQuery): MutableList<FreightInvoiceTier> {
    return query.apply(store.list())
}
