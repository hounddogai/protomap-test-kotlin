package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps capped coaching digests by code. */
interface CappedCoachingDigestStore {
    fun find(code: String): CappedCoachingDigest?
    fun save(item: CappedCoachingDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCoachingDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCoachingDigestStore(
    var items: MutableMap<String, CappedCoachingDigest>,
    var order: MutableList<String>,
) : CappedCoachingDigestStore {
    override fun find(code: String): CappedCoachingDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCoachingDigest) {
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

    override fun list(): MutableList<CappedCoachingDigest> {
        val out = mutableListOf<CappedCoachingDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCoachingDigestStore(): CappedCoachingDigestStore {
    return MemoryCappedCoachingDigestStore(
        mutableMapOf<String, CappedCoachingDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCoachingDigestStore(
    var inner: CappedCoachingDigestStore,
    var cache: TtlCache<CappedCoachingDigest>,
    var hits: Int,
    var misses: Int,
) : CappedCoachingDigestStore {
    override fun find(code: String): CappedCoachingDigest? {
        val hit = (cache.get(code) as CappedCoachingDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCoachingDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCoachingDigest> = inner.list()
}

fun newCachedCappedCoachingDigestStore(inner: CappedCoachingDigestStore, ttl: Int, clock: Clock): CappedCoachingDigestStore {
    return CachedCappedCoachingDigestStore(
        inner = inner,
        cache = newTtlCache<CappedCoachingDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped coaching digests. Empty fields match all. */
data class CappedCoachingDigestQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCoachingDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedCoachingDigest>): MutableList<CappedCoachingDigest> {
        val matched = filter(items) { candidate: CappedCoachingDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCoachingDigests(store: CappedCoachingDigestStore, query: CappedCoachingDigestQuery): MutableList<CappedCoachingDigest> {
    return query.apply(store.list())
}
