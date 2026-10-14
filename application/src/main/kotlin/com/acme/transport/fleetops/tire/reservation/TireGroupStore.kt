package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire groups by code. */
interface TireGroupStore {
    fun find(code: String): TireGroup?
    fun save(item: TireGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireGroupStore(
    var items: MutableMap<String, TireGroup>,
    var order: MutableList<String>,
) : TireGroupStore {
    override fun find(code: String): TireGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireGroup) {
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

    override fun list(): MutableList<TireGroup> {
        val out = mutableListOf<TireGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireGroupStore(): TireGroupStore {
    return MemoryTireGroupStore(mutableMapOf<String, TireGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireGroupStore(
    var inner: TireGroupStore,
    var cache: TtlCache<TireGroup>,
    var hits: Int,
    var misses: Int,
) : TireGroupStore {
    override fun find(code: String): TireGroup? {
        val hit = (cache.get(code) as TireGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireGroup> = inner.list()
}

fun newCachedTireGroupStore(inner: TireGroupStore, ttl: Int, clock: Clock): TireGroupStore {
    return CachedTireGroupStore(
        inner = inner,
        cache = newTtlCache<TireGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire groups. Empty fields match all. */
data class TireGroupQuery(
    var depot: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireGroup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireGroup>): MutableList<TireGroup> {
        val matched = filter(items) { candidate: TireGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireGroups(store: TireGroupStore, query: TireGroupQuery): MutableList<TireGroup> {
    return query.apply(store.list())
}
