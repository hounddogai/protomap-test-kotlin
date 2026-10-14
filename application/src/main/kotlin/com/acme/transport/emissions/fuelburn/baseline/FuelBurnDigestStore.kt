package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn digests by code. */
interface FuelBurnDigestStore {
    fun find(code: String): FuelBurnDigest?
    fun save(item: FuelBurnDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnDigestStore(
    var items: MutableMap<String, FuelBurnDigest>,
    var order: MutableList<String>,
) : FuelBurnDigestStore {
    override fun find(code: String): FuelBurnDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnDigest) {
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

    override fun list(): MutableList<FuelBurnDigest> {
        val out = mutableListOf<FuelBurnDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnDigestStore(): FuelBurnDigestStore {
    return MemoryFuelBurnDigestStore(mutableMapOf<String, FuelBurnDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnDigestStore(
    var inner: FuelBurnDigestStore,
    var cache: TtlCache<FuelBurnDigest>,
    var hits: Int,
    var misses: Int,
) : FuelBurnDigestStore {
    override fun find(code: String): FuelBurnDigest? {
        val hit = (cache.get(code) as FuelBurnDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnDigest> = inner.list()
}

fun newCachedFuelBurnDigestStore(inner: FuelBurnDigestStore, ttl: Int, clock: Clock): FuelBurnDigestStore {
    return CachedFuelBurnDigestStore(
        inner = inner,
        cache = newTtlCache<FuelBurnDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn digests. Empty fields match all. */
data class FuelBurnDigestQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FuelBurnDigest>): MutableList<FuelBurnDigest> {
        val matched = filter(items) { candidate: FuelBurnDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnDigests(store: FuelBurnDigestStore, query: FuelBurnDigestQuery): MutableList<FuelBurnDigest> {
    return query.apply(store.list())
}
