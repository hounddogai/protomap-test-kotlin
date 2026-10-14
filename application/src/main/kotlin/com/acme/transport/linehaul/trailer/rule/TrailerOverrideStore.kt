package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer overrides by code. */
interface TrailerOverrideStore {
    fun find(code: String): TrailerOverride?
    fun save(item: TrailerOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerOverrideStore(
    var items: MutableMap<String, TrailerOverride>,
    var order: MutableList<String>,
) : TrailerOverrideStore {
    override fun find(code: String): TrailerOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerOverride) {
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

    override fun list(): MutableList<TrailerOverride> {
        val out = mutableListOf<TrailerOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerOverrideStore(): TrailerOverrideStore {
    return MemoryTrailerOverrideStore(mutableMapOf<String, TrailerOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerOverrideStore(
    var inner: TrailerOverrideStore,
    var cache: TtlCache<TrailerOverride>,
    var hits: Int,
    var misses: Int,
) : TrailerOverrideStore {
    override fun find(code: String): TrailerOverride? {
        val hit = (cache.get(code) as TrailerOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerOverride> = inner.list()
}

fun newCachedTrailerOverrideStore(inner: TrailerOverrideStore, ttl: Int, clock: Clock): TrailerOverrideStore {
    return CachedTrailerOverrideStore(
        inner = inner,
        cache = newTtlCache<TrailerOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer overrides. Empty fields match all. */
data class TrailerOverrideQuery(
    var unit: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerOverride): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerOverride>): MutableList<TrailerOverride> {
        val matched = filter(items) { candidate: TrailerOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerOverrides(store: TrailerOverrideStore, query: TrailerOverrideQuery): MutableList<TrailerOverride> {
    return query.apply(store.list())
}
