package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted attempt digests by code. */
interface WeightedAttemptDigestStore {
    fun find(code: String): WeightedAttemptDigest?
    fun save(item: WeightedAttemptDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAttemptDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAttemptDigestStore(
    var items: MutableMap<String, WeightedAttemptDigest>,
    var order: MutableList<String>,
) : WeightedAttemptDigestStore {
    override fun find(code: String): WeightedAttemptDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAttemptDigest) {
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

    override fun list(): MutableList<WeightedAttemptDigest> {
        val out = mutableListOf<WeightedAttemptDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAttemptDigestStore(): WeightedAttemptDigestStore {
    return MemoryWeightedAttemptDigestStore(
        mutableMapOf<String, WeightedAttemptDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAttemptDigestStore(
    var inner: WeightedAttemptDigestStore,
    var cache: TtlCache<WeightedAttemptDigest>,
    var hits: Int,
    var misses: Int,
) : WeightedAttemptDigestStore {
    override fun find(code: String): WeightedAttemptDigest? {
        val hit = (cache.get(code) as WeightedAttemptDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAttemptDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAttemptDigest> = inner.list()
}

fun newCachedWeightedAttemptDigestStore(inner: WeightedAttemptDigestStore, ttl: Int, clock: Clock): WeightedAttemptDigestStore {
    return CachedWeightedAttemptDigestStore(
        inner = inner,
        cache = newTtlCache<WeightedAttemptDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted attempt digests. Empty fields match all. */
data class WeightedAttemptDigestQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAttemptDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<WeightedAttemptDigest>): MutableList<WeightedAttemptDigest> {
        val matched = filter(items) { candidate: WeightedAttemptDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAttemptDigests(store: WeightedAttemptDigestStore, query: WeightedAttemptDigestQuery): MutableList<WeightedAttemptDigest> {
    return query.apply(store.list())
}
