package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel burn digests by code. */
interface GlobalFuelBurnDigestStore {
    fun find(code: String): GlobalFuelBurnDigest?
    fun save(item: GlobalFuelBurnDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelBurnDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelBurnDigestStore(
    var items: MutableMap<String, GlobalFuelBurnDigest>,
    var order: MutableList<String>,
) : GlobalFuelBurnDigestStore {
    override fun find(code: String): GlobalFuelBurnDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelBurnDigest) {
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

    override fun list(): MutableList<GlobalFuelBurnDigest> {
        val out = mutableListOf<GlobalFuelBurnDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelBurnDigestStore(): GlobalFuelBurnDigestStore {
    return MemoryGlobalFuelBurnDigestStore(
        mutableMapOf<String, GlobalFuelBurnDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelBurnDigestStore(
    var inner: GlobalFuelBurnDigestStore,
    var cache: TtlCache<GlobalFuelBurnDigest>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelBurnDigestStore {
    override fun find(code: String): GlobalFuelBurnDigest? {
        val hit = (cache.get(code) as GlobalFuelBurnDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelBurnDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelBurnDigest> = inner.list()
}

fun newCachedGlobalFuelBurnDigestStore(inner: GlobalFuelBurnDigestStore, ttl: Int, clock: Clock): GlobalFuelBurnDigestStore {
    return CachedGlobalFuelBurnDigestStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelBurnDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel burn digests. Empty fields match all. */
data class GlobalFuelBurnDigestQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelBurnDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFuelBurnDigest>): MutableList<GlobalFuelBurnDigest> {
        val matched = filter(items) { candidate: GlobalFuelBurnDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelBurnDigests(store: GlobalFuelBurnDigestStore, query: GlobalFuelBurnDigestQuery): MutableList<GlobalFuelBurnDigest> {
    return query.apply(store.list())
}
