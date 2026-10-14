package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient service plan digests by code. */
interface LenientServicePlanDigestStore {
    fun find(code: String): LenientServicePlanDigest?
    fun save(item: LenientServicePlanDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientServicePlanDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientServicePlanDigestStore(
    var items: MutableMap<String, LenientServicePlanDigest>,
    var order: MutableList<String>,
) : LenientServicePlanDigestStore {
    override fun find(code: String): LenientServicePlanDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientServicePlanDigest) {
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

    override fun list(): MutableList<LenientServicePlanDigest> {
        val out = mutableListOf<LenientServicePlanDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientServicePlanDigestStore(): LenientServicePlanDigestStore {
    return MemoryLenientServicePlanDigestStore(
        mutableMapOf<String, LenientServicePlanDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientServicePlanDigestStore(
    var inner: LenientServicePlanDigestStore,
    var cache: TtlCache<LenientServicePlanDigest>,
    var hits: Int,
    var misses: Int,
) : LenientServicePlanDigestStore {
    override fun find(code: String): LenientServicePlanDigest? {
        val hit = (cache.get(code) as LenientServicePlanDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientServicePlanDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientServicePlanDigest> = inner.list()
}

fun newCachedLenientServicePlanDigestStore(inner: LenientServicePlanDigestStore, ttl: Int, clock: Clock): LenientServicePlanDigestStore {
    return CachedLenientServicePlanDigestStore(
        inner = inner,
        cache = newTtlCache<LenientServicePlanDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient service plan digests. Empty fields match all. */
data class LenientServicePlanDigestQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientServicePlanDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientServicePlanDigest>): MutableList<LenientServicePlanDigest> {
        val matched = filter(items) { candidate: LenientServicePlanDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientServicePlanDigests(store: LenientServicePlanDigestStore, query: LenientServicePlanDigestQuery): MutableList<LenientServicePlanDigest> {
    return query.apply(store.list())
}
