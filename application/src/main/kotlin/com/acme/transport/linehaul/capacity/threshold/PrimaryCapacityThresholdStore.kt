package com.acme.transport.linehaul.capacity.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary capacity thresholds by code. */
interface PrimaryCapacityThresholdStore {
    fun find(code: String): PrimaryCapacityThreshold?
    fun save(item: PrimaryCapacityThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCapacityThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCapacityThresholdStore(
    var items: MutableMap<String, PrimaryCapacityThreshold>,
    var order: MutableList<String>,
) : PrimaryCapacityThresholdStore {
    override fun find(code: String): PrimaryCapacityThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCapacityThreshold) {
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

    override fun list(): MutableList<PrimaryCapacityThreshold> {
        val out = mutableListOf<PrimaryCapacityThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCapacityThresholdStore(): PrimaryCapacityThresholdStore {
    return MemoryPrimaryCapacityThresholdStore(
        mutableMapOf<String, PrimaryCapacityThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCapacityThresholdStore(
    var inner: PrimaryCapacityThresholdStore,
    var cache: TtlCache<PrimaryCapacityThreshold>,
    var hits: Int,
    var misses: Int,
) : PrimaryCapacityThresholdStore {
    override fun find(code: String): PrimaryCapacityThreshold? {
        val hit = (cache.get(code) as PrimaryCapacityThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCapacityThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCapacityThreshold> = inner.list()
}

fun newCachedPrimaryCapacityThresholdStore(inner: PrimaryCapacityThresholdStore, ttl: Int, clock: Clock): PrimaryCapacityThresholdStore {
    return CachedPrimaryCapacityThresholdStore(
        inner = inner,
        cache = newTtlCache<PrimaryCapacityThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary capacity thresholds. Empty fields match all. */
data class PrimaryCapacityThresholdQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCapacityThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryCapacityThreshold>): MutableList<PrimaryCapacityThreshold> {
        val matched = filter(items) { candidate: PrimaryCapacityThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCapacityThresholds(store: PrimaryCapacityThresholdStore, query: PrimaryCapacityThresholdQuery): MutableList<PrimaryCapacityThreshold> {
    return query.apply(store.list())
}
