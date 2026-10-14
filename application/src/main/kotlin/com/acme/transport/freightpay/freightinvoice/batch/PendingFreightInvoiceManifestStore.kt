package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending freight invoice manifests by code. */
interface PendingFreightInvoiceManifestStore {
    fun find(code: String): PendingFreightInvoiceManifest?
    fun save(item: PendingFreightInvoiceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFreightInvoiceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFreightInvoiceManifestStore(
    var items: MutableMap<String, PendingFreightInvoiceManifest>,
    var order: MutableList<String>,
) : PendingFreightInvoiceManifestStore {
    override fun find(code: String): PendingFreightInvoiceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFreightInvoiceManifest) {
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

    override fun list(): MutableList<PendingFreightInvoiceManifest> {
        val out = mutableListOf<PendingFreightInvoiceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFreightInvoiceManifestStore(): PendingFreightInvoiceManifestStore {
    return MemoryPendingFreightInvoiceManifestStore(
        mutableMapOf<String, PendingFreightInvoiceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFreightInvoiceManifestStore(
    var inner: PendingFreightInvoiceManifestStore,
    var cache: TtlCache<PendingFreightInvoiceManifest>,
    var hits: Int,
    var misses: Int,
) : PendingFreightInvoiceManifestStore {
    override fun find(code: String): PendingFreightInvoiceManifest? {
        val hit = (cache.get(code) as PendingFreightInvoiceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFreightInvoiceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFreightInvoiceManifest> = inner.list()
}

fun newCachedPendingFreightInvoiceManifestStore(inner: PendingFreightInvoiceManifestStore, ttl: Int, clock: Clock): PendingFreightInvoiceManifestStore {
    return CachedPendingFreightInvoiceManifestStore(
        inner = inner,
        cache = newTtlCache<PendingFreightInvoiceManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending freight invoice manifests. Empty fields match all. */
data class PendingFreightInvoiceManifestQuery(
    var label: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFreightInvoiceManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<PendingFreightInvoiceManifest>): MutableList<PendingFreightInvoiceManifest> {
        val matched = filter(items) { candidate: PendingFreightInvoiceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFreightInvoiceManifests(store: PendingFreightInvoiceManifestStore, query: PendingFreightInvoiceManifestQuery): MutableList<PendingFreightInvoiceManifest> {
    return query.apply(store.list())
}
