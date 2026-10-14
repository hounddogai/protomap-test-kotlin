package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability rollups by code. */
interface AvailabilityRollupStore {
    fun find(code: String): AvailabilityRollup?
    fun save(item: AvailabilityRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityRollupStore(
    var items: MutableMap<String, AvailabilityRollup>,
    var order: MutableList<String>,
) : AvailabilityRollupStore {
    override fun find(code: String): AvailabilityRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityRollup) {
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

    override fun list(): MutableList<AvailabilityRollup> {
        val out = mutableListOf<AvailabilityRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityRollupStore(): AvailabilityRollupStore {
    return MemoryAvailabilityRollupStore(mutableMapOf<String, AvailabilityRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityRollupStore(
    var inner: AvailabilityRollupStore,
    var cache: TtlCache<AvailabilityRollup>,
    var hits: Int,
    var misses: Int,
) : AvailabilityRollupStore {
    override fun find(code: String): AvailabilityRollup? {
        val hit = (cache.get(code) as AvailabilityRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityRollup> = inner.list()
}

fun newCachedAvailabilityRollupStore(inner: AvailabilityRollupStore, ttl: Int, clock: Clock): AvailabilityRollupStore {
    return CachedAvailabilityRollupStore(
        inner = inner,
        cache = newTtlCache<AvailabilityRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability rollups. Empty fields match all. */
data class AvailabilityRollupQuery(
    var lane: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityRollup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityRollup>): MutableList<AvailabilityRollup> {
        val matched = filter(items) { candidate: AvailabilityRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityRollups(store: AvailabilityRollupStore, query: AvailabilityRollupQuery): MutableList<AvailabilityRollup> {
    return query.apply(store.list())
}
