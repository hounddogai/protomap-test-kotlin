package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal freight invoice manifests by code. */
interface SeasonalFreightInvoiceManifestStore {
    fun find(code: String): SeasonalFreightInvoiceManifest?
    fun save(item: SeasonalFreightInvoiceManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalFreightInvoiceManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalFreightInvoiceManifestStore(
    var items: MutableMap<String, SeasonalFreightInvoiceManifest>,
    var order: MutableList<String>,
) : SeasonalFreightInvoiceManifestStore {
    override fun find(code: String): SeasonalFreightInvoiceManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalFreightInvoiceManifest) {
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

    override fun list(): MutableList<SeasonalFreightInvoiceManifest> {
        val out = mutableListOf<SeasonalFreightInvoiceManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalFreightInvoiceManifestStore(): SeasonalFreightInvoiceManifestStore {
    return MemorySeasonalFreightInvoiceManifestStore(
        mutableMapOf<String, SeasonalFreightInvoiceManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalFreightInvoiceManifestStore(
    var inner: SeasonalFreightInvoiceManifestStore,
    var cache: TtlCache<SeasonalFreightInvoiceManifest>,
    var hits: Int,
    var misses: Int,
) : SeasonalFreightInvoiceManifestStore {
    override fun find(code: String): SeasonalFreightInvoiceManifest? {
        val hit = (cache.get(code) as SeasonalFreightInvoiceManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalFreightInvoiceManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalFreightInvoiceManifest> = inner.list()
}

fun newCachedSeasonalFreightInvoiceManifestStore(inner: SeasonalFreightInvoiceManifestStore, ttl: Int, clock: Clock): SeasonalFreightInvoiceManifestStore {
    return CachedSeasonalFreightInvoiceManifestStore(
        inner = inner,
        cache = newTtlCache<SeasonalFreightInvoiceManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal freight invoice manifests. Empty fields match all. */
data class SeasonalFreightInvoiceManifestQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalFreightInvoiceManifest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalFreightInvoiceManifest>): MutableList<SeasonalFreightInvoiceManifest> {
        val matched = filter(items) { candidate: SeasonalFreightInvoiceManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalFreightInvoiceManifests(store: SeasonalFreightInvoiceManifestStore, query: SeasonalFreightInvoiceManifestQuery): MutableList<SeasonalFreightInvoiceManifest> {
    return query.apply(store.list())
}
