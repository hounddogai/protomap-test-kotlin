package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability overrides by code. */
interface AvailabilityOverrideStore {
    fun find(code: String): AvailabilityOverride?
    fun save(item: AvailabilityOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityOverrideStore(
    var items: MutableMap<String, AvailabilityOverride>,
    var order: MutableList<String>,
) : AvailabilityOverrideStore {
    override fun find(code: String): AvailabilityOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityOverride) {
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

    override fun list(): MutableList<AvailabilityOverride> {
        val out = mutableListOf<AvailabilityOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityOverrideStore(): AvailabilityOverrideStore {
    return MemoryAvailabilityOverrideStore(
        mutableMapOf<String, AvailabilityOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityOverrideStore(
    var inner: AvailabilityOverrideStore,
    var cache: TtlCache<AvailabilityOverride>,
    var hits: Int,
    var misses: Int,
) : AvailabilityOverrideStore {
    override fun find(code: String): AvailabilityOverride? {
        val hit = (cache.get(code) as AvailabilityOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityOverride> = inner.list()
}

fun newCachedAvailabilityOverrideStore(inner: AvailabilityOverrideStore, ttl: Int, clock: Clock): AvailabilityOverrideStore {
    return CachedAvailabilityOverrideStore(
        inner = inner,
        cache = newTtlCache<AvailabilityOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability overrides. Empty fields match all. */
data class AvailabilityOverrideQuery(
    var category: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityOverride): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AvailabilityOverride>): MutableList<AvailabilityOverride> {
        val matched = filter(items) { candidate: AvailabilityOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityOverrides(store: AvailabilityOverrideStore, query: AvailabilityOverrideQuery): MutableList<AvailabilityOverride> {
    return query.apply(store.list())
}
