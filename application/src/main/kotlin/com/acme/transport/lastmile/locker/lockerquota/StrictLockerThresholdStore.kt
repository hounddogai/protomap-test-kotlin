package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict locker thresholds by code. */
interface StrictLockerThresholdStore {
    fun find(code: String): StrictLockerThreshold?
    fun save(item: StrictLockerThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLockerThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLockerThresholdStore(
    var items: MutableMap<String, StrictLockerThreshold>,
    var order: MutableList<String>,
) : StrictLockerThresholdStore {
    override fun find(code: String): StrictLockerThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLockerThreshold) {
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

    override fun list(): MutableList<StrictLockerThreshold> {
        val out = mutableListOf<StrictLockerThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLockerThresholdStore(): StrictLockerThresholdStore {
    return MemoryStrictLockerThresholdStore(
        mutableMapOf<String, StrictLockerThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLockerThresholdStore(
    var inner: StrictLockerThresholdStore,
    var cache: TtlCache<StrictLockerThreshold>,
    var hits: Int,
    var misses: Int,
) : StrictLockerThresholdStore {
    override fun find(code: String): StrictLockerThreshold? {
        val hit = (cache.get(code) as StrictLockerThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLockerThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLockerThreshold> = inner.list()
}

fun newCachedStrictLockerThresholdStore(inner: StrictLockerThresholdStore, ttl: Int, clock: Clock): StrictLockerThresholdStore {
    return CachedStrictLockerThresholdStore(
        inner = inner,
        cache = newTtlCache<StrictLockerThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict locker thresholds. Empty fields match all. */
data class StrictLockerThresholdQuery(
    var country: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLockerThreshold): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLockerThreshold>): MutableList<StrictLockerThreshold> {
        val matched = filter(items) { candidate: StrictLockerThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLockerThresholds(store: StrictLockerThresholdStore, query: StrictLockerThresholdQuery): MutableList<StrictLockerThreshold> {
    return query.apply(store.list())
}
