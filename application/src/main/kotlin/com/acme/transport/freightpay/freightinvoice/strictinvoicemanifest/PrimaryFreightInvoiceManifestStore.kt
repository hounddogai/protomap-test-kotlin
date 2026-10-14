package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary freight invoice manifests by code. */
interface PrimaryFreightInvoiceManifestStore {
    fun find(code: String): PrimaryFreightInvoiceManifest?
    fun save(item: PrimaryFreightInvoiceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFreightInvoiceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFreightInvoiceManifestStore(
    var items: MutableMap<String, PrimaryFreightInvoiceManifest>,
    var order: MutableList<String>,
) : PrimaryFreightInvoiceManifestStore {
    override fun find(code: String): PrimaryFreightInvoiceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFreightInvoiceManifest) {
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

    override fun list(): MutableList<PrimaryFreightInvoiceManifest> {
        val out = mutableListOf<PrimaryFreightInvoiceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFreightInvoiceManifestStore(): PrimaryFreightInvoiceManifestStore {
    return MemoryPrimaryFreightInvoiceManifestStore(
        mutableMapOf<String, PrimaryFreightInvoiceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFreightInvoiceManifestStore(
    var inner: PrimaryFreightInvoiceManifestStore,
    var cache: TtlCache<PrimaryFreightInvoiceManifest>,
    var hits: Int,
    var misses: Int,
) : PrimaryFreightInvoiceManifestStore {
    override fun find(code: String): PrimaryFreightInvoiceManifest? {
        val hit = (cache.get(code) as PrimaryFreightInvoiceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFreightInvoiceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFreightInvoiceManifest> = inner.list()
}

fun newCachedPrimaryFreightInvoiceManifestStore(inner: PrimaryFreightInvoiceManifestStore, ttl: Int, clock: Clock): PrimaryFreightInvoiceManifestStore {
    return CachedPrimaryFreightInvoiceManifestStore(
        inner = inner,
        cache = newTtlCache<PrimaryFreightInvoiceManifest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary freight invoice manifests. Empty fields match all. */
data class PrimaryFreightInvoiceManifestQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFreightInvoiceManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFreightInvoiceManifest>): MutableList<PrimaryFreightInvoiceManifest> {
        val matched = filter(items) { candidate: PrimaryFreightInvoiceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFreightInvoiceManifests(store: PrimaryFreightInvoiceManifestStore, query: PrimaryFreightInvoiceManifestQuery): MutableList<PrimaryFreightInvoiceManifest> {
    return query.apply(store.list())
}
