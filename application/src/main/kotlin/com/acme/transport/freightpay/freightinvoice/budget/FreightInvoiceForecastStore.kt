package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice forecasts by code. */
interface FreightInvoiceForecastStore {
    fun find(code: String): FreightInvoiceForecast?
    fun save(item: FreightInvoiceForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceForecastStore(
    var items: MutableMap<String, FreightInvoiceForecast>,
    var order: MutableList<String>,
) : FreightInvoiceForecastStore {
    override fun find(code: String): FreightInvoiceForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceForecast) {
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

    override fun list(): MutableList<FreightInvoiceForecast> {
        val out = mutableListOf<FreightInvoiceForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceForecastStore(): FreightInvoiceForecastStore {
    return MemoryFreightInvoiceForecastStore(
        mutableMapOf<String, FreightInvoiceForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceForecastStore(
    var inner: FreightInvoiceForecastStore,
    var cache: TtlCache<FreightInvoiceForecast>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceForecastStore {
    override fun find(code: String): FreightInvoiceForecast? {
        val hit = (cache.get(code) as FreightInvoiceForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceForecast> = inner.list()
}

fun newCachedFreightInvoiceForecastStore(inner: FreightInvoiceForecastStore, ttl: Int, clock: Clock): FreightInvoiceForecastStore {
    return CachedFreightInvoiceForecastStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice forecasts. Empty fields match all. */
data class FreightInvoiceForecastQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceForecast>): MutableList<FreightInvoiceForecast> {
        val matched = filter(items) { candidate: FreightInvoiceForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceForecasts(store: FreightInvoiceForecastStore, query: FreightInvoiceForecastQuery): MutableList<FreightInvoiceForecast> {
    return query.apply(store.list())
}
