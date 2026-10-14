package com.acme.transport.crewing.availability.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability groups by code. */
interface AvailabilityGroupStore {
    fun find(code: String): AvailabilityGroup?
    fun save(item: AvailabilityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityGroupStore(
    var items: MutableMap<String, AvailabilityGroup>,
    var order: MutableList<String>,
) : AvailabilityGroupStore {
    override fun find(code: String): AvailabilityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityGroup) {
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

    override fun list(): MutableList<AvailabilityGroup> {
        val out = mutableListOf<AvailabilityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityGroupStore(): AvailabilityGroupStore {
    return MemoryAvailabilityGroupStore(mutableMapOf<String, AvailabilityGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityGroupStore(
    var inner: AvailabilityGroupStore,
    var cache: TtlCache<AvailabilityGroup>,
    var hits: Int,
    var misses: Int,
) : AvailabilityGroupStore {
    override fun find(code: String): AvailabilityGroup? {
        val hit = (cache.get(code) as AvailabilityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityGroup> = inner.list()
}

fun newCachedAvailabilityGroupStore(inner: AvailabilityGroupStore, ttl: Int, clock: Clock): AvailabilityGroupStore {
    return CachedAvailabilityGroupStore(
        inner = inner,
        cache = newTtlCache<AvailabilityGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability groups. Empty fields match all. */
data class AvailabilityGroupQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AvailabilityGroup>): MutableList<AvailabilityGroup> {
        val matched = filter(items) { candidate: AvailabilityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityGroups(store: AvailabilityGroupStore, query: AvailabilityGroupQuery): MutableList<AvailabilityGroup> {
    return query.apply(store.list())
}
