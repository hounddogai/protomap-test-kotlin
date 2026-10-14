package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling freight invoice quotas by code. */
interface RollingFreightInvoiceQuotaStore {
    fun find(code: String): RollingFreightInvoiceQuota?
    fun save(item: RollingFreightInvoiceQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFreightInvoiceQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFreightInvoiceQuotaStore(
    var items: MutableMap<String, RollingFreightInvoiceQuota>,
    var order: MutableList<String>,
) : RollingFreightInvoiceQuotaStore {
    override fun find(code: String): RollingFreightInvoiceQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFreightInvoiceQuota) {
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

    override fun list(): MutableList<RollingFreightInvoiceQuota> {
        val out = mutableListOf<RollingFreightInvoiceQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFreightInvoiceQuotaStore(): RollingFreightInvoiceQuotaStore {
    return MemoryRollingFreightInvoiceQuotaStore(
        mutableMapOf<String, RollingFreightInvoiceQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFreightInvoiceQuotaStore(
    var inner: RollingFreightInvoiceQuotaStore,
    var cache: TtlCache<RollingFreightInvoiceQuota>,
    var hits: Int,
    var misses: Int,
) : RollingFreightInvoiceQuotaStore {
    override fun find(code: String): RollingFreightInvoiceQuota? {
        val hit = (cache.get(code) as RollingFreightInvoiceQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFreightInvoiceQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFreightInvoiceQuota> = inner.list()
}

fun newCachedRollingFreightInvoiceQuotaStore(inner: RollingFreightInvoiceQuotaStore, ttl: Int, clock: Clock): RollingFreightInvoiceQuotaStore {
    return CachedRollingFreightInvoiceQuotaStore(
        inner = inner,
        cache = newTtlCache<RollingFreightInvoiceQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling freight invoice quotas. Empty fields match all. */
data class RollingFreightInvoiceQuotaQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFreightInvoiceQuota): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingFreightInvoiceQuota>): MutableList<RollingFreightInvoiceQuota> {
        val matched = filter(items) { candidate: RollingFreightInvoiceQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFreightInvoiceQuotas(store: RollingFreightInvoiceQuotaStore, query: RollingFreightInvoiceQuotaQuery): MutableList<RollingFreightInvoiceQuota> {
    return query.apply(store.list())
}
