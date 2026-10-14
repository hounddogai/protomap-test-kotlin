package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice manifests by code. */
interface FreightInvoiceManifestStore {
    fun find(code: String): FreightInvoiceManifest?
    fun save(item: FreightInvoiceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceManifestStore(
    var items: MutableMap<String, FreightInvoiceManifest>,
    var order: MutableList<String>,
) : FreightInvoiceManifestStore {
    override fun find(code: String): FreightInvoiceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceManifest) {
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

    override fun list(): MutableList<FreightInvoiceManifest> {
        val out = mutableListOf<FreightInvoiceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceManifestStore(): FreightInvoiceManifestStore {
    return MemoryFreightInvoiceManifestStore(
        mutableMapOf<String, FreightInvoiceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceManifestStore(
    var inner: FreightInvoiceManifestStore,
    var cache: TtlCache<FreightInvoiceManifest>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceManifestStore {
    override fun find(code: String): FreightInvoiceManifest? {
        val hit = (cache.get(code) as FreightInvoiceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceManifest> = inner.list()
}

fun newCachedFreightInvoiceManifestStore(inner: FreightInvoiceManifestStore, ttl: Int, clock: Clock): FreightInvoiceManifestStore {
    return CachedFreightInvoiceManifestStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice manifests. Empty fields match all. */
data class FreightInvoiceManifestQuery(
    var title: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceManifest>): MutableList<FreightInvoiceManifest> {
        val matched = filter(items) { candidate: FreightInvoiceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceManifests(store: FreightInvoiceManifestStore, query: FreightInvoiceManifestQuery): MutableList<FreightInvoiceManifest> {
    return query.apply(store.list())
}
