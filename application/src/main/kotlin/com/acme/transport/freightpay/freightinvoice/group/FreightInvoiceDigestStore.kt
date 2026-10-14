package com.acme.transport.freightpay.freightinvoice.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice digests by code. */
interface FreightInvoiceDigestStore {
    fun find(code: String): FreightInvoiceDigest?
    fun save(item: FreightInvoiceDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceDigestStore(
    var items: MutableMap<String, FreightInvoiceDigest>,
    var order: MutableList<String>,
) : FreightInvoiceDigestStore {
    override fun find(code: String): FreightInvoiceDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceDigest) {
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

    override fun list(): MutableList<FreightInvoiceDigest> {
        val out = mutableListOf<FreightInvoiceDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceDigestStore(): FreightInvoiceDigestStore {
    return MemoryFreightInvoiceDigestStore(
        mutableMapOf<String, FreightInvoiceDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceDigestStore(
    var inner: FreightInvoiceDigestStore,
    var cache: TtlCache<FreightInvoiceDigest>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceDigestStore {
    override fun find(code: String): FreightInvoiceDigest? {
        val hit = (cache.get(code) as FreightInvoiceDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceDigest> = inner.list()
}

fun newCachedFreightInvoiceDigestStore(inner: FreightInvoiceDigestStore, ttl: Int, clock: Clock): FreightInvoiceDigestStore {
    return CachedFreightInvoiceDigestStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice digests. Empty fields match all. */
data class FreightInvoiceDigestQuery(
    var segment: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceDigest): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceDigest>): MutableList<FreightInvoiceDigest> {
        val matched = filter(items) { candidate: FreightInvoiceDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceDigests(store: FreightInvoiceDigestStore, query: FreightInvoiceDigestQuery): MutableList<FreightInvoiceDigest> {
    return query.apply(store.list())
}
