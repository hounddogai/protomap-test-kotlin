package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered inspection groups by code. */
interface TieredInspectionGroupStore {
    fun find(code: String): TieredInspectionGroup?
    fun save(item: TieredInspectionGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredInspectionGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredInspectionGroupStore(
    var items: MutableMap<String, TieredInspectionGroup>,
    var order: MutableList<String>,
) : TieredInspectionGroupStore {
    override fun find(code: String): TieredInspectionGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredInspectionGroup) {
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

    override fun list(): MutableList<TieredInspectionGroup> {
        val out = mutableListOf<TieredInspectionGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredInspectionGroupStore(): TieredInspectionGroupStore {
    return MemoryTieredInspectionGroupStore(
        mutableMapOf<String, TieredInspectionGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredInspectionGroupStore(
    var inner: TieredInspectionGroupStore,
    var cache: TtlCache<TieredInspectionGroup>,
    var hits: Int,
    var misses: Int,
) : TieredInspectionGroupStore {
    override fun find(code: String): TieredInspectionGroup? {
        val hit = (cache.get(code) as TieredInspectionGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredInspectionGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredInspectionGroup> = inner.list()
}

fun newCachedTieredInspectionGroupStore(inner: TieredInspectionGroupStore, ttl: Int, clock: Clock): TieredInspectionGroupStore {
    return CachedTieredInspectionGroupStore(
        inner = inner,
        cache = newTtlCache<TieredInspectionGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered inspection groups. Empty fields match all. */
data class TieredInspectionGroupQuery(
    var region: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredInspectionGroup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredInspectionGroup>): MutableList<TieredInspectionGroup> {
        val matched = filter(items) { candidate: TieredInspectionGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredInspectionGroups(store: TieredInspectionGroupStore, query: TieredInspectionGroupQuery): MutableList<TieredInspectionGroup> {
    return query.apply(store.list())
}
