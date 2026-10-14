package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps primary service area thresholds by code. */
interface PrimaryServiceAreaThresholdStore {
    fun find(code: String): PrimaryServiceAreaThreshold?
    fun save(item: PrimaryServiceAreaThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryServiceAreaThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryServiceAreaThresholdStore(
    var items: MutableMap<String, PrimaryServiceAreaThreshold>,
    var order: MutableList<String>,
) : PrimaryServiceAreaThresholdStore {
    override fun find(code: String): PrimaryServiceAreaThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryServiceAreaThreshold) {
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

    override fun list(): MutableList<PrimaryServiceAreaThreshold> {
        val out = mutableListOf<PrimaryServiceAreaThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryServiceAreaThresholdStore(): PrimaryServiceAreaThresholdStore {
    return MemoryPrimaryServiceAreaThresholdStore(
        mutableMapOf<String, PrimaryServiceAreaThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryServiceAreaThresholdStore(
    var inner: PrimaryServiceAreaThresholdStore,
    var cache: TtlCache<PrimaryServiceAreaThreshold>,
    var hits: Int,
    var misses: Int,
) : PrimaryServiceAreaThresholdStore {
    override fun find(code: String): PrimaryServiceAreaThreshold? {
        val hit = (cache.get(code) as PrimaryServiceAreaThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryServiceAreaThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryServiceAreaThreshold> = inner.list()
}

fun newCachedPrimaryServiceAreaThresholdStore(inner: PrimaryServiceAreaThresholdStore, ttl: Int, clock: Clock): PrimaryServiceAreaThresholdStore {
    return CachedPrimaryServiceAreaThresholdStore(
        inner = inner,
        cache = newTtlCache<PrimaryServiceAreaThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary service area thresholds. Empty fields match all. */
data class PrimaryServiceAreaThresholdQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryServiceAreaThreshold): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryServiceAreaThreshold>): MutableList<PrimaryServiceAreaThreshold> {
        val matched = filter(items) { candidate: PrimaryServiceAreaThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryServiceAreaThresholds(store: PrimaryServiceAreaThresholdStore, query: PrimaryServiceAreaThresholdQuery): MutableList<PrimaryServiceAreaThreshold> {
    return query.apply(store.list())
}
