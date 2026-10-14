package com.acme.transport.network.servicearea.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional service area thresholds by code. */
interface RegionalServiceAreaThresholdStore {
    fun find(code: String): RegionalServiceAreaThreshold?
    fun save(item: RegionalServiceAreaThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServiceAreaThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServiceAreaThresholdStore(
    var items: MutableMap<String, RegionalServiceAreaThreshold>,
    var order: MutableList<String>,
) : RegionalServiceAreaThresholdStore {
    override fun find(code: String): RegionalServiceAreaThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServiceAreaThreshold) {
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

    override fun list(): MutableList<RegionalServiceAreaThreshold> {
        val out = mutableListOf<RegionalServiceAreaThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServiceAreaThresholdStore(): RegionalServiceAreaThresholdStore {
    return MemoryRegionalServiceAreaThresholdStore(
        mutableMapOf<String, RegionalServiceAreaThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServiceAreaThresholdStore(
    var inner: RegionalServiceAreaThresholdStore,
    var cache: TtlCache<RegionalServiceAreaThreshold>,
    var hits: Int,
    var misses: Int,
) : RegionalServiceAreaThresholdStore {
    override fun find(code: String): RegionalServiceAreaThreshold? {
        val hit = (cache.get(code) as RegionalServiceAreaThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServiceAreaThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServiceAreaThreshold> = inner.list()
}

fun newCachedRegionalServiceAreaThresholdStore(inner: RegionalServiceAreaThresholdStore, ttl: Int, clock: Clock): RegionalServiceAreaThresholdStore {
    return CachedRegionalServiceAreaThresholdStore(
        inner = inner,
        cache = newTtlCache<RegionalServiceAreaThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service area thresholds. Empty fields match all. */
data class RegionalServiceAreaThresholdQuery(
    var category: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServiceAreaThreshold): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalServiceAreaThreshold>): MutableList<RegionalServiceAreaThreshold> {
        val matched = filter(items) { candidate: RegionalServiceAreaThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServiceAreaThresholds(store: RegionalServiceAreaThresholdStore, query: RegionalServiceAreaThresholdQuery): MutableList<RegionalServiceAreaThreshold> {
    return query.apply(store.list())
}
