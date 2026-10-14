package com.acme.transport.lastmile.locker.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted locker profiles by code. */
interface WeightedLockerProfileStore {
    fun find(code: String): WeightedLockerProfile?
    fun save(item: WeightedLockerProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLockerProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLockerProfileStore(
    var items: MutableMap<String, WeightedLockerProfile>,
    var order: MutableList<String>,
) : WeightedLockerProfileStore {
    override fun find(code: String): WeightedLockerProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLockerProfile) {
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

    override fun list(): MutableList<WeightedLockerProfile> {
        val out = mutableListOf<WeightedLockerProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLockerProfileStore(): WeightedLockerProfileStore {
    return MemoryWeightedLockerProfileStore(
        mutableMapOf<String, WeightedLockerProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLockerProfileStore(
    var inner: WeightedLockerProfileStore,
    var cache: TtlCache<WeightedLockerProfile>,
    var hits: Int,
    var misses: Int,
) : WeightedLockerProfileStore {
    override fun find(code: String): WeightedLockerProfile? {
        val hit = (cache.get(code) as WeightedLockerProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLockerProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLockerProfile> = inner.list()
}

fun newCachedWeightedLockerProfileStore(inner: WeightedLockerProfileStore, ttl: Int, clock: Clock): WeightedLockerProfileStore {
    return CachedWeightedLockerProfileStore(
        inner = inner,
        cache = newTtlCache<WeightedLockerProfile>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted locker profiles. Empty fields match all. */
data class WeightedLockerProfileQuery(
    var segment: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLockerProfile): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<WeightedLockerProfile>): MutableList<WeightedLockerProfile> {
        val matched = filter(items) { candidate: WeightedLockerProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLockerProfiles(store: WeightedLockerProfileStore, query: WeightedLockerProfileQuery): MutableList<WeightedLockerProfile> {
    return query.apply(store.list())
}
