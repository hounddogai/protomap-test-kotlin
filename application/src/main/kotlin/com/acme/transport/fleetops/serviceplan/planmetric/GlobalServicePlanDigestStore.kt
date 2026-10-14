package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global service plan digests by code. */
interface GlobalServicePlanDigestStore {
    fun find(code: String): GlobalServicePlanDigest?
    fun save(item: GlobalServicePlanDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalServicePlanDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalServicePlanDigestStore(
    var items: MutableMap<String, GlobalServicePlanDigest>,
    var order: MutableList<String>,
) : GlobalServicePlanDigestStore {
    override fun find(code: String): GlobalServicePlanDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalServicePlanDigest) {
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

    override fun list(): MutableList<GlobalServicePlanDigest> {
        val out = mutableListOf<GlobalServicePlanDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalServicePlanDigestStore(): GlobalServicePlanDigestStore {
    return MemoryGlobalServicePlanDigestStore(
        mutableMapOf<String, GlobalServicePlanDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalServicePlanDigestStore(
    var inner: GlobalServicePlanDigestStore,
    var cache: TtlCache<GlobalServicePlanDigest>,
    var hits: Int,
    var misses: Int,
) : GlobalServicePlanDigestStore {
    override fun find(code: String): GlobalServicePlanDigest? {
        val hit = (cache.get(code) as GlobalServicePlanDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalServicePlanDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalServicePlanDigest> = inner.list()
}

fun newCachedGlobalServicePlanDigestStore(inner: GlobalServicePlanDigestStore, ttl: Int, clock: Clock): GlobalServicePlanDigestStore {
    return CachedGlobalServicePlanDigestStore(
        inner = inner,
        cache = newTtlCache<GlobalServicePlanDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global service plan digests. Empty fields match all. */
data class GlobalServicePlanDigestQuery(
    var lane: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalServicePlanDigest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalServicePlanDigest>): MutableList<GlobalServicePlanDigest> {
        val matched = filter(items) { candidate: GlobalServicePlanDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalServicePlanDigests(store: GlobalServicePlanDigestStore, query: GlobalServicePlanDigestQuery): MutableList<GlobalServicePlanDigest> {
    return query.apply(store.list())
}
