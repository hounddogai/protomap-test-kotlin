package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive cross dock thresholds by code. */
interface AdaptiveCrossDockThresholdStore {
    fun find(code: String): AdaptiveCrossDockThreshold?
    fun save(item: AdaptiveCrossDockThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCrossDockThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCrossDockThresholdStore(
    var items: MutableMap<String, AdaptiveCrossDockThreshold>,
    var order: MutableList<String>,
) : AdaptiveCrossDockThresholdStore {
    override fun find(code: String): AdaptiveCrossDockThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCrossDockThreshold) {
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

    override fun list(): MutableList<AdaptiveCrossDockThreshold> {
        val out = mutableListOf<AdaptiveCrossDockThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCrossDockThresholdStore(): AdaptiveCrossDockThresholdStore {
    return MemoryAdaptiveCrossDockThresholdStore(
        mutableMapOf<String, AdaptiveCrossDockThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCrossDockThresholdStore(
    var inner: AdaptiveCrossDockThresholdStore,
    var cache: TtlCache<AdaptiveCrossDockThreshold>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCrossDockThresholdStore {
    override fun find(code: String): AdaptiveCrossDockThreshold? {
        val hit = (cache.get(code) as AdaptiveCrossDockThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCrossDockThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCrossDockThreshold> = inner.list()
}

fun newCachedAdaptiveCrossDockThresholdStore(inner: AdaptiveCrossDockThresholdStore, ttl: Int, clock: Clock): AdaptiveCrossDockThresholdStore {
    return CachedAdaptiveCrossDockThresholdStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCrossDockThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive cross dock thresholds. Empty fields match all. */
data class AdaptiveCrossDockThresholdQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCrossDockThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AdaptiveCrossDockThreshold>): MutableList<AdaptiveCrossDockThreshold> {
        val matched = filter(items) { candidate: AdaptiveCrossDockThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCrossDockThresholds(store: AdaptiveCrossDockThresholdStore, query: AdaptiveCrossDockThresholdQuery): MutableList<AdaptiveCrossDockThreshold> {
    return query.apply(store.list())
}
