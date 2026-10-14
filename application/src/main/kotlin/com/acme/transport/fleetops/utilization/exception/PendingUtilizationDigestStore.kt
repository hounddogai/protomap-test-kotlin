package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending utilization digests by code. */
interface PendingUtilizationDigestStore {
    fun find(code: String): PendingUtilizationDigest?
    fun save(item: PendingUtilizationDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingUtilizationDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingUtilizationDigestStore(
    var items: MutableMap<String, PendingUtilizationDigest>,
    var order: MutableList<String>,
) : PendingUtilizationDigestStore {
    override fun find(code: String): PendingUtilizationDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingUtilizationDigest) {
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

    override fun list(): MutableList<PendingUtilizationDigest> {
        val out = mutableListOf<PendingUtilizationDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingUtilizationDigestStore(): PendingUtilizationDigestStore {
    return MemoryPendingUtilizationDigestStore(
        mutableMapOf<String, PendingUtilizationDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingUtilizationDigestStore(
    var inner: PendingUtilizationDigestStore,
    var cache: TtlCache<PendingUtilizationDigest>,
    var hits: Int,
    var misses: Int,
) : PendingUtilizationDigestStore {
    override fun find(code: String): PendingUtilizationDigest? {
        val hit = (cache.get(code) as PendingUtilizationDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingUtilizationDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingUtilizationDigest> = inner.list()
}

fun newCachedPendingUtilizationDigestStore(inner: PendingUtilizationDigestStore, ttl: Int, clock: Clock): PendingUtilizationDigestStore {
    return CachedPendingUtilizationDigestStore(
        inner = inner,
        cache = newTtlCache<PendingUtilizationDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending utilization digests. Empty fields match all. */
data class PendingUtilizationDigestQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingUtilizationDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingUtilizationDigest>): MutableList<PendingUtilizationDigest> {
        val matched = filter(items) { candidate: PendingUtilizationDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingUtilizationDigests(store: PendingUtilizationDigestStore, query: PendingUtilizationDigestQuery): MutableList<PendingUtilizationDigest> {
    return query.apply(store.list())
}
