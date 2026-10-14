package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted attempt profiles by code. */
interface WeightedAttemptProfileStore {
    fun find(code: String): WeightedAttemptProfile?
    fun save(item: WeightedAttemptProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAttemptProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAttemptProfileStore(
    var items: MutableMap<String, WeightedAttemptProfile>,
    var order: MutableList<String>,
) : WeightedAttemptProfileStore {
    override fun find(code: String): WeightedAttemptProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAttemptProfile) {
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

    override fun list(): MutableList<WeightedAttemptProfile> {
        val out = mutableListOf<WeightedAttemptProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAttemptProfileStore(): WeightedAttemptProfileStore {
    return MemoryWeightedAttemptProfileStore(
        mutableMapOf<String, WeightedAttemptProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAttemptProfileStore(
    var inner: WeightedAttemptProfileStore,
    var cache: TtlCache<WeightedAttemptProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedAttemptProfileStore {
    override fun find(code: String): WeightedAttemptProfile? {
        val hit = (cache.get(code) as WeightedAttemptProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAttemptProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAttemptProfile> = inner.list()
}

fun newCachedWeightedAttemptProfileStore(inner: WeightedAttemptProfileStore, ttl: Int, clock: Clock): WeightedAttemptProfileStore {
    return CachedWeightedAttemptProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedAttemptProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted attempt profiles. Empty fields match all. */
data class WeightedAttemptProfileQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAttemptProfile): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<WeightedAttemptProfile>): MutableList<WeightedAttemptProfile> {
        val matched = filter(items) { candidate: WeightedAttemptProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAttemptProfiles(store: WeightedAttemptProfileStore, query: WeightedAttemptProfileQuery): MutableList<WeightedAttemptProfile> {
    return query.apply(store.list())
}
