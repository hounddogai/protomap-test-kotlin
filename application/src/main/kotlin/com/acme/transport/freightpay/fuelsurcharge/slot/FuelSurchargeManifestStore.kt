package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge manifests by code. */
interface FuelSurchargeManifestStore {
    fun find(code: String): FuelSurchargeManifest?
    fun save(item: FuelSurchargeManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeManifestStore(
    var items: MutableMap<String, FuelSurchargeManifest>,
    var order: MutableList<String>,
) : FuelSurchargeManifestStore {
    override fun find(code: String): FuelSurchargeManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeManifest) {
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

    override fun list(): MutableList<FuelSurchargeManifest> {
        val out = mutableListOf<FuelSurchargeManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeManifestStore(): FuelSurchargeManifestStore {
    return MemoryFuelSurchargeManifestStore(
        mutableMapOf<String, FuelSurchargeManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeManifestStore(
    var inner: FuelSurchargeManifestStore,
    var cache: TtlCache<FuelSurchargeManifest>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeManifestStore {
    override fun find(code: String): FuelSurchargeManifest? {
        val hit = (cache.get(code) as FuelSurchargeManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeManifest> = inner.list()
}

fun newCachedFuelSurchargeManifestStore(inner: FuelSurchargeManifestStore, ttl: Int, clock: Clock): FuelSurchargeManifestStore {
    return CachedFuelSurchargeManifestStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge manifests. Empty fields match all. */
data class FuelSurchargeManifestQuery(
    var label: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FuelSurchargeManifest>): MutableList<FuelSurchargeManifest> {
        val matched = filter(items) { candidate: FuelSurchargeManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeManifests(store: FuelSurchargeManifestStore, query: FuelSurchargeManifestQuery): MutableList<FuelSurchargeManifest> {
    return query.apply(store.list())
}
