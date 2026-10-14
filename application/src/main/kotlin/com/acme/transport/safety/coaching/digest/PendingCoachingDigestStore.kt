package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending coaching digests by code. */
interface PendingCoachingDigestStore {
    fun find(code: String): PendingCoachingDigest?
    fun save(item: PendingCoachingDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCoachingDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCoachingDigestStore(
    var items: MutableMap<String, PendingCoachingDigest>,
    var order: MutableList<String>,
) : PendingCoachingDigestStore {
    override fun find(code: String): PendingCoachingDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCoachingDigest) {
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

    override fun list(): MutableList<PendingCoachingDigest> {
        val out = mutableListOf<PendingCoachingDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCoachingDigestStore(): PendingCoachingDigestStore {
    return MemoryPendingCoachingDigestStore(
        mutableMapOf<String, PendingCoachingDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCoachingDigestStore(
    var inner: PendingCoachingDigestStore,
    var cache: TtlCache<PendingCoachingDigest>,
    var hits: Int,
    var misses: Int,
) : PendingCoachingDigestStore {
    override fun find(code: String): PendingCoachingDigest? {
        val hit = (cache.get(code) as PendingCoachingDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCoachingDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCoachingDigest> = inner.list()
}

fun newCachedPendingCoachingDigestStore(inner: PendingCoachingDigestStore, ttl: Int, clock: Clock): PendingCoachingDigestStore {
    return CachedPendingCoachingDigestStore(
        inner = inner,
        cache = newTtlCache<PendingCoachingDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending coaching digests. Empty fields match all. */
data class PendingCoachingDigestQuery(
    var region: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCoachingDigest): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCoachingDigest>): MutableList<PendingCoachingDigest> {
        val matched = filter(items) { candidate: PendingCoachingDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCoachingDigests(store: PendingCoachingDigestStore, query: PendingCoachingDigestQuery): MutableList<PendingCoachingDigest> {
    return query.apply(store.list())
}
