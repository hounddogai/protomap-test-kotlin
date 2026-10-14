package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered ev charge digests by code. */
interface TieredEvChargeDigestStore {
    fun find(code: String): TieredEvChargeDigest?
    fun save(item: TieredEvChargeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEvChargeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEvChargeDigestStore(
    var items: MutableMap<String, TieredEvChargeDigest>,
    var order: MutableList<String>,
) : TieredEvChargeDigestStore {
    override fun find(code: String): TieredEvChargeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEvChargeDigest) {
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

    override fun list(): MutableList<TieredEvChargeDigest> {
        val out = mutableListOf<TieredEvChargeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEvChargeDigestStore(): TieredEvChargeDigestStore {
    return MemoryTieredEvChargeDigestStore(
        mutableMapOf<String, TieredEvChargeDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEvChargeDigestStore(
    var inner: TieredEvChargeDigestStore,
    var cache: TtlCache<TieredEvChargeDigest>,
    var hits: Int,
    var misses: Int,
) : TieredEvChargeDigestStore {
    override fun find(code: String): TieredEvChargeDigest? {
        val hit = (cache.get(code) as TieredEvChargeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEvChargeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEvChargeDigest> = inner.list()
}

fun newCachedTieredEvChargeDigestStore(inner: TieredEvChargeDigestStore, ttl: Int, clock: Clock): TieredEvChargeDigestStore {
    return CachedTieredEvChargeDigestStore(
        inner = inner,
        cache = newTtlCache<TieredEvChargeDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered ev charge digests. Empty fields match all. */
data class TieredEvChargeDigestQuery(
    var region: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEvChargeDigest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredEvChargeDigest>): MutableList<TieredEvChargeDigest> {
        val matched = filter(items) { candidate: TieredEvChargeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEvChargeDigests(store: TieredEvChargeDigestStore, query: TieredEvChargeDigestQuery): MutableList<TieredEvChargeDigest> {
    return query.apply(store.list())
}
