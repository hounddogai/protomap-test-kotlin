package com.acme.transport.fleetops.utilization.group

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered utilization groups by code. */
interface TieredUtilizationGroupStore {
    fun find(code: String): TieredUtilizationGroup?
    fun save(item: TieredUtilizationGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredUtilizationGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredUtilizationGroupStore(
    var items: MutableMap<String, TieredUtilizationGroup>,
    var order: MutableList<String>,
) : TieredUtilizationGroupStore {
    override fun find(code: String): TieredUtilizationGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredUtilizationGroup) {
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

    override fun list(): MutableList<TieredUtilizationGroup> {
        val out = mutableListOf<TieredUtilizationGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredUtilizationGroupStore(): TieredUtilizationGroupStore {
    return MemoryTieredUtilizationGroupStore(
        mutableMapOf<String, TieredUtilizationGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredUtilizationGroupStore(
    var inner: TieredUtilizationGroupStore,
    var cache: TtlCache<TieredUtilizationGroup>,
    var hits: Int,
    var misses: Int,
) : TieredUtilizationGroupStore {
    override fun find(code: String): TieredUtilizationGroup? {
        val hit = (cache.get(code) as TieredUtilizationGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredUtilizationGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredUtilizationGroup> = inner.list()
}

fun newCachedTieredUtilizationGroupStore(inner: TieredUtilizationGroupStore, ttl: Int, clock: Clock): TieredUtilizationGroupStore {
    return CachedTieredUtilizationGroupStore(
        inner = inner,
        cache = newTtlCache<TieredUtilizationGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered utilization groups. Empty fields match all. */
data class TieredUtilizationGroupQuery(
    var segment: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredUtilizationGroup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TieredUtilizationGroup>): MutableList<TieredUtilizationGroup> {
        val matched = filter(items) { candidate: TieredUtilizationGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredUtilizationGroups(store: TieredUtilizationGroupStore, query: TieredUtilizationGroupQuery): MutableList<TieredUtilizationGroup> {
    return query.apply(store.list())
}
