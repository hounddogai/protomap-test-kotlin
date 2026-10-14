package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps primary scorecard digests by code. */
interface PrimaryScorecardDigestStore {
    fun find(code: String): PrimaryScorecardDigest?
    fun save(item: PrimaryScorecardDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryScorecardDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryScorecardDigestStore(
    var items: MutableMap<String, PrimaryScorecardDigest>,
    var order: MutableList<String>,
) : PrimaryScorecardDigestStore {
    override fun find(code: String): PrimaryScorecardDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryScorecardDigest) {
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

    override fun list(): MutableList<PrimaryScorecardDigest> {
        val out = mutableListOf<PrimaryScorecardDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryScorecardDigestStore(): PrimaryScorecardDigestStore {
    return MemoryPrimaryScorecardDigestStore(
        mutableMapOf<String, PrimaryScorecardDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryScorecardDigestStore(
    var inner: PrimaryScorecardDigestStore,
    var cache: TtlCache<PrimaryScorecardDigest>,
    var hits: Int,
    var misses: Int,
) : PrimaryScorecardDigestStore {
    override fun find(code: String): PrimaryScorecardDigest? {
        val hit = (cache.get(code) as PrimaryScorecardDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryScorecardDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryScorecardDigest> = inner.list()
}

fun newCachedPrimaryScorecardDigestStore(inner: PrimaryScorecardDigestStore, ttl: Int, clock: Clock): PrimaryScorecardDigestStore {
    return CachedPrimaryScorecardDigestStore(
        inner = inner,
        cache = newTtlCache<PrimaryScorecardDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary scorecard digests. Empty fields match all. */
data class PrimaryScorecardDigestQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryScorecardDigest): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryScorecardDigest>): MutableList<PrimaryScorecardDigest> {
        val matched = filter(items) { candidate: PrimaryScorecardDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryScorecardDigests(store: PrimaryScorecardDigestStore, query: PrimaryScorecardDigestQuery): MutableList<PrimaryScorecardDigest> {
    return query.apply(store.list())
}
