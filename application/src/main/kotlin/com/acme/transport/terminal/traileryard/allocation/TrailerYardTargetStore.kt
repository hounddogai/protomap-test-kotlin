package com.acme.transport.terminal.traileryard.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard targets by code. */
interface TrailerYardTargetStore {
    fun find(code: String): TrailerYardTarget?
    fun save(item: TrailerYardTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardTargetStore(
    var items: MutableMap<String, TrailerYardTarget>,
    var order: MutableList<String>,
) : TrailerYardTargetStore {
    override fun find(code: String): TrailerYardTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardTarget) {
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

    override fun list(): MutableList<TrailerYardTarget> {
        val out = mutableListOf<TrailerYardTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardTargetStore(): TrailerYardTargetStore {
    return MemoryTrailerYardTargetStore(mutableMapOf<String, TrailerYardTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardTargetStore(
    var inner: TrailerYardTargetStore,
    var cache: TtlCache<TrailerYardTarget>,
    var hits: Int,
    var misses: Int,
) : TrailerYardTargetStore {
    override fun find(code: String): TrailerYardTarget? {
        val hit = (cache.get(code) as TrailerYardTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardTarget> = inner.list()
}

fun newCachedTrailerYardTargetStore(inner: TrailerYardTargetStore, ttl: Int, clock: Clock): TrailerYardTargetStore {
    return CachedTrailerYardTargetStore(
        inner = inner,
        cache = newTtlCache<TrailerYardTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard targets. Empty fields match all. */
data class TrailerYardTargetQuery(
    var segment: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardTarget): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardTarget>): MutableList<TrailerYardTarget> {
        val matched = filter(items) { candidate: TrailerYardTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardTargets(store: TrailerYardTargetStore, query: TrailerYardTargetQuery): MutableList<TrailerYardTarget> {
    return query.apply(store.list())
}
