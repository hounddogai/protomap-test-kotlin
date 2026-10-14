package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy freight invoice digests by code. */
interface LegacyFreightInvoiceDigestStore {
    fun find(code: String): LegacyFreightInvoiceDigest?
    fun save(item: LegacyFreightInvoiceDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyFreightInvoiceDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyFreightInvoiceDigestStore(
    var items: MutableMap<String, LegacyFreightInvoiceDigest>,
    var order: MutableList<String>,
) : LegacyFreightInvoiceDigestStore {
    override fun find(code: String): LegacyFreightInvoiceDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyFreightInvoiceDigest) {
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

    override fun list(): MutableList<LegacyFreightInvoiceDigest> {
        val out = mutableListOf<LegacyFreightInvoiceDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyFreightInvoiceDigestStore(): LegacyFreightInvoiceDigestStore {
    return MemoryLegacyFreightInvoiceDigestStore(
        mutableMapOf<String, LegacyFreightInvoiceDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyFreightInvoiceDigestStore(
    var inner: LegacyFreightInvoiceDigestStore,
    var cache: TtlCache<LegacyFreightInvoiceDigest>,
    var hits: Int,
    var misses: Int,
) : LegacyFreightInvoiceDigestStore {
    override fun find(code: String): LegacyFreightInvoiceDigest? {
        val hit = (cache.get(code) as LegacyFreightInvoiceDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyFreightInvoiceDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyFreightInvoiceDigest> = inner.list()
}

fun newCachedLegacyFreightInvoiceDigestStore(inner: LegacyFreightInvoiceDigestStore, ttl: Int, clock: Clock): LegacyFreightInvoiceDigestStore {
    return CachedLegacyFreightInvoiceDigestStore(
        inner = inner,
        cache = newTtlCache<LegacyFreightInvoiceDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy freight invoice digests. Empty fields match all. */
data class LegacyFreightInvoiceDigestQuery(
    var country: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyFreightInvoiceDigest): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyFreightInvoiceDigest>): MutableList<LegacyFreightInvoiceDigest> {
        val matched = filter(items) { candidate: LegacyFreightInvoiceDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyFreightInvoiceDigests(store: LegacyFreightInvoiceDigestStore, query: LegacyFreightInvoiceDigestQuery): MutableList<LegacyFreightInvoiceDigest> {
    return query.apply(store.list())
}
