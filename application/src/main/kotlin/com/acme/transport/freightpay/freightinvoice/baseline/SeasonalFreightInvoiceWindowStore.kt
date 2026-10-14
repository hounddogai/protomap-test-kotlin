package com.acme.transport.freightpay.freightinvoice.baseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal freight invoice windows by code. */
interface SeasonalFreightInvoiceWindowStore {
    fun find(code: String): SeasonalFreightInvoiceWindow?
    fun save(item: SeasonalFreightInvoiceWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFreightInvoiceWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFreightInvoiceWindowStore(
    var items: MutableMap<String, SeasonalFreightInvoiceWindow>,
    var order: MutableList<String>,
) : SeasonalFreightInvoiceWindowStore {
    override fun find(code: String): SeasonalFreightInvoiceWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFreightInvoiceWindow) {
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

    override fun list(): MutableList<SeasonalFreightInvoiceWindow> {
        val out = mutableListOf<SeasonalFreightInvoiceWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFreightInvoiceWindowStore(): SeasonalFreightInvoiceWindowStore {
    return MemorySeasonalFreightInvoiceWindowStore(
        mutableMapOf<String, SeasonalFreightInvoiceWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFreightInvoiceWindowStore(
    var inner: SeasonalFreightInvoiceWindowStore,
    var cache: TtlCache<SeasonalFreightInvoiceWindow>,
    var hits: Int,
    var misses: Int,
) : SeasonalFreightInvoiceWindowStore {
    override fun find(code: String): SeasonalFreightInvoiceWindow? {
        val hit = (cache.get(code) as SeasonalFreightInvoiceWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFreightInvoiceWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFreightInvoiceWindow> = inner.list()
}

fun newCachedSeasonalFreightInvoiceWindowStore(inner: SeasonalFreightInvoiceWindowStore, ttl: Int, clock: Clock): SeasonalFreightInvoiceWindowStore {
    return CachedSeasonalFreightInvoiceWindowStore(
        inner = inner,
        cache = newTtlCache<SeasonalFreightInvoiceWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal freight invoice windows. Empty fields match all. */
data class SeasonalFreightInvoiceWindowQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFreightInvoiceWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFreightInvoiceWindow>): MutableList<SeasonalFreightInvoiceWindow> {
        val matched = filter(items) { candidate: SeasonalFreightInvoiceWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFreightInvoiceWindows(store: SeasonalFreightInvoiceWindowStore, query: SeasonalFreightInvoiceWindowQuery): MutableList<SeasonalFreightInvoiceWindow> {
    return query.apply(store.list())
}
