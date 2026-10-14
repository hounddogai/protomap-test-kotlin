package com.acme.transport.network.transittime.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time thresholds by code. */
interface TransitTimeThresholdStore {
    fun find(code: String): TransitTimeThreshold?
    fun save(item: TransitTimeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeThresholdStore(
    var items: MutableMap<String, TransitTimeThreshold>,
    var order: MutableList<String>,
) : TransitTimeThresholdStore {
    override fun find(code: String): TransitTimeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeThreshold) {
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

    override fun list(): MutableList<TransitTimeThreshold> {
        val out = mutableListOf<TransitTimeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeThresholdStore(): TransitTimeThresholdStore {
    return MemoryTransitTimeThresholdStore(
        mutableMapOf<String, TransitTimeThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeThresholdStore(
    var inner: TransitTimeThresholdStore,
    var cache: TtlCache<TransitTimeThreshold>,
    var hits: Int,
    var misses: Int,
) : TransitTimeThresholdStore {
    override fun find(code: String): TransitTimeThreshold? {
        val hit = (cache.get(code) as TransitTimeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeThreshold> = inner.list()
}

fun newCachedTransitTimeThresholdStore(inner: TransitTimeThresholdStore, ttl: Int, clock: Clock): TransitTimeThresholdStore {
    return CachedTransitTimeThresholdStore(
        inner = inner,
        cache = newTtlCache<TransitTimeThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time thresholds. Empty fields match all. */
data class TransitTimeThresholdQuery(
    var title: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TransitTimeThreshold>): MutableList<TransitTimeThreshold> {
        val matched = filter(items) { candidate: TransitTimeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeThresholds(store: TransitTimeThresholdStore, query: TransitTimeThresholdQuery): MutableList<TransitTimeThreshold> {
    return query.apply(store.list())
}
