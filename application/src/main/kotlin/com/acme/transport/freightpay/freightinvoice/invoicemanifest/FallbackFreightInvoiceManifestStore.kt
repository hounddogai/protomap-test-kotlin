package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback freight invoice manifests by code. */
interface FallbackFreightInvoiceManifestStore {
    fun find(code: String): FallbackFreightInvoiceManifest?
    fun save(item: FallbackFreightInvoiceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackFreightInvoiceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackFreightInvoiceManifestStore(
    var items: MutableMap<String, FallbackFreightInvoiceManifest>,
    var order: MutableList<String>,
) : FallbackFreightInvoiceManifestStore {
    override fun find(code: String): FallbackFreightInvoiceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackFreightInvoiceManifest) {
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

    override fun list(): MutableList<FallbackFreightInvoiceManifest> {
        val out = mutableListOf<FallbackFreightInvoiceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackFreightInvoiceManifestStore(): FallbackFreightInvoiceManifestStore {
    return MemoryFallbackFreightInvoiceManifestStore(
        mutableMapOf<String, FallbackFreightInvoiceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackFreightInvoiceManifestStore(
    var inner: FallbackFreightInvoiceManifestStore,
    var cache: TtlCache<FallbackFreightInvoiceManifest>,
    var hits: Int,
    var misses: Int,
) : FallbackFreightInvoiceManifestStore {
    override fun find(code: String): FallbackFreightInvoiceManifest? {
        val hit = (cache.get(code) as FallbackFreightInvoiceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackFreightInvoiceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackFreightInvoiceManifest> = inner.list()
}

fun newCachedFallbackFreightInvoiceManifestStore(inner: FallbackFreightInvoiceManifestStore, ttl: Int, clock: Clock): FallbackFreightInvoiceManifestStore {
    return CachedFallbackFreightInvoiceManifestStore(
        inner = inner,
        cache = newTtlCache<FallbackFreightInvoiceManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback freight invoice manifests. Empty fields match all. */
data class FallbackFreightInvoiceManifestQuery(
    var source: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackFreightInvoiceManifest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackFreightInvoiceManifest>): MutableList<FallbackFreightInvoiceManifest> {
        val matched = filter(items) { candidate: FallbackFreightInvoiceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackFreightInvoiceManifests(store: FallbackFreightInvoiceManifestStore, query: FallbackFreightInvoiceManifestQuery): MutableList<FallbackFreightInvoiceManifest> {
    return query.apply(store.list())
}
