package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered freight invoice quotas by code. */
interface TieredFreightInvoiceQuotaStore {
    fun find(code: String): TieredFreightInvoiceQuota?
    fun save(item: TieredFreightInvoiceQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFreightInvoiceQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFreightInvoiceQuotaStore(
    var items: MutableMap<String, TieredFreightInvoiceQuota>,
    var order: MutableList<String>,
) : TieredFreightInvoiceQuotaStore {
    override fun find(code: String): TieredFreightInvoiceQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFreightInvoiceQuota) {
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

    override fun list(): MutableList<TieredFreightInvoiceQuota> {
        val out = mutableListOf<TieredFreightInvoiceQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFreightInvoiceQuotaStore(): TieredFreightInvoiceQuotaStore {
    return MemoryTieredFreightInvoiceQuotaStore(
        mutableMapOf<String, TieredFreightInvoiceQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFreightInvoiceQuotaStore(
    var inner: TieredFreightInvoiceQuotaStore,
    var cache: TtlCache<TieredFreightInvoiceQuota>,
    var hits: Int,
    var misses: Int,
) : TieredFreightInvoiceQuotaStore {
    override fun find(code: String): TieredFreightInvoiceQuota? {
        val hit = (cache.get(code) as TieredFreightInvoiceQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFreightInvoiceQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFreightInvoiceQuota> = inner.list()
}

fun newCachedTieredFreightInvoiceQuotaStore(inner: TieredFreightInvoiceQuotaStore, ttl: Int, clock: Clock): TieredFreightInvoiceQuotaStore {
    return CachedTieredFreightInvoiceQuotaStore(
        inner = inner,
        cache = newTtlCache<TieredFreightInvoiceQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered freight invoice quotas. Empty fields match all. */
data class TieredFreightInvoiceQuotaQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFreightInvoiceQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredFreightInvoiceQuota>): MutableList<TieredFreightInvoiceQuota> {
        val matched = filter(items) { candidate: TieredFreightInvoiceQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFreightInvoiceQuotas(store: TieredFreightInvoiceQuotaStore, query: TieredFreightInvoiceQuotaQuery): MutableList<TieredFreightInvoiceQuota> {
    return query.apply(store.list())
}
