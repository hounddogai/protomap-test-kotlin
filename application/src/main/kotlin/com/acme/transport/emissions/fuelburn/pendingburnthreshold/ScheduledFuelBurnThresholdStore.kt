package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled fuel burn thresholds by code. */
interface ScheduledFuelBurnThresholdStore {
    fun find(code: String): ScheduledFuelBurnThreshold?
    fun save(item: ScheduledFuelBurnThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledFuelBurnThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledFuelBurnThresholdStore(
    var items: MutableMap<String, ScheduledFuelBurnThreshold>,
    var order: MutableList<String>,
) : ScheduledFuelBurnThresholdStore {
    override fun find(code: String): ScheduledFuelBurnThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledFuelBurnThreshold) {
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

    override fun list(): MutableList<ScheduledFuelBurnThreshold> {
        val out = mutableListOf<ScheduledFuelBurnThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledFuelBurnThresholdStore(): ScheduledFuelBurnThresholdStore {
    return MemoryScheduledFuelBurnThresholdStore(
        mutableMapOf<String, ScheduledFuelBurnThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledFuelBurnThresholdStore(
    var inner: ScheduledFuelBurnThresholdStore,
    var cache: TtlCache<ScheduledFuelBurnThreshold>,
    var hits: Int,
    var misses: Int,
) : ScheduledFuelBurnThresholdStore {
    override fun find(code: String): ScheduledFuelBurnThreshold? {
        val hit = (cache.get(code) as ScheduledFuelBurnThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledFuelBurnThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledFuelBurnThreshold> = inner.list()
}

fun newCachedScheduledFuelBurnThresholdStore(inner: ScheduledFuelBurnThresholdStore, ttl: Int, clock: Clock): ScheduledFuelBurnThresholdStore {
    return CachedScheduledFuelBurnThresholdStore(
        inner = inner,
        cache = newTtlCache<ScheduledFuelBurnThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled fuel burn thresholds. Empty fields match all. */
data class ScheduledFuelBurnThresholdQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledFuelBurnThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledFuelBurnThreshold>): MutableList<ScheduledFuelBurnThreshold> {
        val matched = filter(items) { candidate: ScheduledFuelBurnThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledFuelBurnThresholds(store: ScheduledFuelBurnThresholdStore, query: ScheduledFuelBurnThresholdQuery): MutableList<ScheduledFuelBurnThreshold> {
    return query.apply(store.list())
}
