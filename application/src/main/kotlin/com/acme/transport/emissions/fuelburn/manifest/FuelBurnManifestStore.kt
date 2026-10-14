package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn manifests by code. */
interface FuelBurnManifestStore {
    fun find(code: String): FuelBurnManifest?
    fun save(item: FuelBurnManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnManifestStore(
    var items: MutableMap<String, FuelBurnManifest>,
    var order: MutableList<String>,
) : FuelBurnManifestStore {
    override fun find(code: String): FuelBurnManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnManifest) {
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

    override fun list(): MutableList<FuelBurnManifest> {
        val out = mutableListOf<FuelBurnManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnManifestStore(): FuelBurnManifestStore {
    return MemoryFuelBurnManifestStore(mutableMapOf<String, FuelBurnManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnManifestStore(
    var inner: FuelBurnManifestStore,
    var cache: TtlCache<FuelBurnManifest>,
    var hits: Int,
    var misses: Int,
) : FuelBurnManifestStore {
    override fun find(code: String): FuelBurnManifest? {
        val hit = (cache.get(code) as FuelBurnManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnManifest> = inner.list()
}

fun newCachedFuelBurnManifestStore(inner: FuelBurnManifestStore, ttl: Int, clock: Clock): FuelBurnManifestStore {
    return CachedFuelBurnManifestStore(
        inner = inner,
        cache = newTtlCache<FuelBurnManifest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn manifests. Empty fields match all. */
data class FuelBurnManifestQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnManifest>): MutableList<FuelBurnManifest> {
        val matched = filter(items) { candidate: FuelBurnManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnManifests(store: FuelBurnManifestStore, query: FuelBurnManifestQuery): MutableList<FuelBurnManifest> {
    return query.apply(store.list())
}
