package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps fallback parcel thresholds by code. */
interface FallbackParcelThresholdStore {
    fun find(code: String): FallbackParcelThreshold?
    fun save(item: FallbackParcelThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackParcelThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackParcelThresholdStore(
    var items: MutableMap<String, FallbackParcelThreshold>,
    var order: MutableList<String>,
) : FallbackParcelThresholdStore {
    override fun find(code: String): FallbackParcelThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackParcelThreshold) {
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

    override fun list(): MutableList<FallbackParcelThreshold> {
        val out = mutableListOf<FallbackParcelThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackParcelThresholdStore(): FallbackParcelThresholdStore {
    return MemoryFallbackParcelThresholdStore(
        mutableMapOf<String, FallbackParcelThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackParcelThresholdStore(
    var inner: FallbackParcelThresholdStore,
    var cache: TtlCache<FallbackParcelThreshold>,
    var hits: Int,
    var misses: Int,
) : FallbackParcelThresholdStore {
    override fun find(code: String): FallbackParcelThreshold? {
        val hit = (cache.get(code) as FallbackParcelThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackParcelThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackParcelThreshold> = inner.list()
}

fun newCachedFallbackParcelThresholdStore(inner: FallbackParcelThresholdStore, ttl: Int, clock: Clock): FallbackParcelThresholdStore {
    return CachedFallbackParcelThresholdStore(
        inner = inner,
        cache = newTtlCache<FallbackParcelThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback parcel thresholds. Empty fields match all. */
data class FallbackParcelThresholdQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackParcelThreshold): Boolean {
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

    fun apply(items: MutableList<FallbackParcelThreshold>): MutableList<FallbackParcelThreshold> {
        val matched = filter(items) { candidate: FallbackParcelThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackParcelThresholds(store: FallbackParcelThresholdStore, query: FallbackParcelThresholdQuery): MutableList<FallbackParcelThreshold> {
    return query.apply(store.list())
}
