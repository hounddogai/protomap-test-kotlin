package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional availability groups by code. */
interface RegionalAvailabilityGroupStore {
    fun find(code: String): RegionalAvailabilityGroup?
    fun save(item: RegionalAvailabilityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAvailabilityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAvailabilityGroupStore(
    var items: MutableMap<String, RegionalAvailabilityGroup>,
    var order: MutableList<String>,
) : RegionalAvailabilityGroupStore {
    override fun find(code: String): RegionalAvailabilityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAvailabilityGroup) {
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

    override fun list(): MutableList<RegionalAvailabilityGroup> {
        val out = mutableListOf<RegionalAvailabilityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAvailabilityGroupStore(): RegionalAvailabilityGroupStore {
    return MemoryRegionalAvailabilityGroupStore(
        mutableMapOf<String, RegionalAvailabilityGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAvailabilityGroupStore(
    var inner: RegionalAvailabilityGroupStore,
    var cache: TtlCache<RegionalAvailabilityGroup>,
    var hits: Int,
    var misses: Int,
) : RegionalAvailabilityGroupStore {
    override fun find(code: String): RegionalAvailabilityGroup? {
        val hit = (cache.get(code) as RegionalAvailabilityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAvailabilityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAvailabilityGroup> = inner.list()
}

fun newCachedRegionalAvailabilityGroupStore(inner: RegionalAvailabilityGroupStore, ttl: Int, clock: Clock): RegionalAvailabilityGroupStore {
    return CachedRegionalAvailabilityGroupStore(
        inner = inner,
        cache = newTtlCache<RegionalAvailabilityGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional availability groups. Empty fields match all. */
data class RegionalAvailabilityGroupQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAvailabilityGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalAvailabilityGroup>): MutableList<RegionalAvailabilityGroup> {
        val matched = filter(items) { candidate: RegionalAvailabilityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAvailabilityGroups(store: RegionalAvailabilityGroupStore, query: RegionalAvailabilityGroupQuery): MutableList<RegionalAvailabilityGroup> {
    return query.apply(store.list())
}
