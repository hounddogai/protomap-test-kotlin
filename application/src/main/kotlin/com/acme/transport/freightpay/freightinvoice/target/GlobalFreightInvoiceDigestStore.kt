package com.acme.transport.freightpay.freightinvoice.target

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global freight invoice digests by code. */
interface GlobalFreightInvoiceDigestStore {
    fun find(code: String): GlobalFreightInvoiceDigest?
    fun save(item: GlobalFreightInvoiceDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFreightInvoiceDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFreightInvoiceDigestStore(
    var items: MutableMap<String, GlobalFreightInvoiceDigest>,
    var order: MutableList<String>,
) : GlobalFreightInvoiceDigestStore {
    override fun find(code: String): GlobalFreightInvoiceDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFreightInvoiceDigest) {
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

    override fun list(): MutableList<GlobalFreightInvoiceDigest> {
        val out = mutableListOf<GlobalFreightInvoiceDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFreightInvoiceDigestStore(): GlobalFreightInvoiceDigestStore {
    return MemoryGlobalFreightInvoiceDigestStore(
        mutableMapOf<String, GlobalFreightInvoiceDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFreightInvoiceDigestStore(
    var inner: GlobalFreightInvoiceDigestStore,
    var cache: TtlCache<GlobalFreightInvoiceDigest>,
    var hits: Int,
    var misses: Int,
) : GlobalFreightInvoiceDigestStore {
    override fun find(code: String): GlobalFreightInvoiceDigest? {
        val hit = (cache.get(code) as GlobalFreightInvoiceDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFreightInvoiceDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFreightInvoiceDigest> = inner.list()
}

fun newCachedGlobalFreightInvoiceDigestStore(inner: GlobalFreightInvoiceDigestStore, ttl: Int, clock: Clock): GlobalFreightInvoiceDigestStore {
    return CachedGlobalFreightInvoiceDigestStore(
        inner = inner,
        cache = newTtlCache<GlobalFreightInvoiceDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global freight invoice digests. Empty fields match all. */
data class GlobalFreightInvoiceDigestQuery(
    var name: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFreightInvoiceDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFreightInvoiceDigest>): MutableList<GlobalFreightInvoiceDigest> {
        val matched = filter(items) { candidate: GlobalFreightInvoiceDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFreightInvoiceDigests(store: GlobalFreightInvoiceDigestStore, query: GlobalFreightInvoiceDigestQuery): MutableList<GlobalFreightInvoiceDigest> {
    return query.apply(store.list())
}
