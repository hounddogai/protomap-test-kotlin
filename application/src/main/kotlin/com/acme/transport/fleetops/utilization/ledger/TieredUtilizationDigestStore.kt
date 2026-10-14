package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered utilization digests by code. */
interface TieredUtilizationDigestStore {
    fun find(code: String): TieredUtilizationDigest?
    fun save(item: TieredUtilizationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredUtilizationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredUtilizationDigestStore(
    var items: MutableMap<String, TieredUtilizationDigest>,
    var order: MutableList<String>,
) : TieredUtilizationDigestStore {
    override fun find(code: String): TieredUtilizationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredUtilizationDigest) {
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

    override fun list(): MutableList<TieredUtilizationDigest> {
        val out = mutableListOf<TieredUtilizationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredUtilizationDigestStore(): TieredUtilizationDigestStore {
    return MemoryTieredUtilizationDigestStore(
        mutableMapOf<String, TieredUtilizationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredUtilizationDigestStore(
    var inner: TieredUtilizationDigestStore,
    var cache: TtlCache<TieredUtilizationDigest>,
    var hits: Int,
    var misses: Int,
) : TieredUtilizationDigestStore {
    override fun find(code: String): TieredUtilizationDigest? {
        val hit = (cache.get(code) as TieredUtilizationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredUtilizationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredUtilizationDigest> = inner.list()
}

fun newCachedTieredUtilizationDigestStore(inner: TieredUtilizationDigestStore, ttl: Int, clock: Clock): TieredUtilizationDigestStore {
    return CachedTieredUtilizationDigestStore(
        inner = inner,
        cache = newTtlCache<TieredUtilizationDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered utilization digests. Empty fields match all. */
data class TieredUtilizationDigestQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredUtilizationDigest): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredUtilizationDigest>): MutableList<TieredUtilizationDigest> {
        val matched = filter(items) { candidate: TieredUtilizationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredUtilizationDigests(store: TieredUtilizationDigestStore, query: TieredUtilizationDigestQuery): MutableList<TieredUtilizationDigest> {
    return query.apply(store.list())
}
