package com.acme.transport.fleetops.tire.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire thresholds by code. */
interface TireThresholdStore {
    fun find(code: String): TireThreshold?
    fun save(item: TireThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireThresholdStore(
    var items: MutableMap<String, TireThreshold>,
    var order: MutableList<String>,
) : TireThresholdStore {
    override fun find(code: String): TireThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireThreshold) {
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

    override fun list(): MutableList<TireThreshold> {
        val out = mutableListOf<TireThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireThresholdStore(): TireThresholdStore {
    return MemoryTireThresholdStore(mutableMapOf<String, TireThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireThresholdStore(
    var inner: TireThresholdStore,
    var cache: TtlCache<TireThreshold>,
    var hits: Int,
    var misses: Int,
) : TireThresholdStore {
    override fun find(code: String): TireThreshold? {
        val hit = (cache.get(code) as TireThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireThreshold> = inner.list()
}

fun newCachedTireThresholdStore(inner: TireThresholdStore, ttl: Int, clock: Clock): TireThresholdStore {
    return CachedTireThresholdStore(
        inner = inner,
        cache = newTtlCache<TireThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire thresholds. Empty fields match all. */
data class TireThresholdQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<TireThreshold>): MutableList<TireThreshold> {
        val matched = filter(items) { candidate: TireThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireThresholds(store: TireThresholdStore, query: TireThresholdQuery): MutableList<TireThreshold> {
    return query.apply(store.list())
}
