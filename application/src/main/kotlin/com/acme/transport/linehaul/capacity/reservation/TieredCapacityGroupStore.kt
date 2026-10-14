package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered capacity groups by code. */
interface TieredCapacityGroupStore {
    fun find(code: String): TieredCapacityGroup?
    fun save(item: TieredCapacityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCapacityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCapacityGroupStore(
    var items: MutableMap<String, TieredCapacityGroup>,
    var order: MutableList<String>,
) : TieredCapacityGroupStore {
    override fun find(code: String): TieredCapacityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCapacityGroup) {
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

    override fun list(): MutableList<TieredCapacityGroup> {
        val out = mutableListOf<TieredCapacityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCapacityGroupStore(): TieredCapacityGroupStore {
    return MemoryTieredCapacityGroupStore(mutableMapOf<String, TieredCapacityGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCapacityGroupStore(
    var inner: TieredCapacityGroupStore,
    var cache: TtlCache<TieredCapacityGroup>,
    var hits: Int,
    var misses: Int,
) : TieredCapacityGroupStore {
    override fun find(code: String): TieredCapacityGroup? {
        val hit = (cache.get(code) as TieredCapacityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCapacityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCapacityGroup> = inner.list()
}

fun newCachedTieredCapacityGroupStore(inner: TieredCapacityGroupStore, ttl: Int, clock: Clock): TieredCapacityGroupStore {
    return CachedTieredCapacityGroupStore(
        inner = inner,
        cache = newTtlCache<TieredCapacityGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered capacity groups. Empty fields match all. */
data class TieredCapacityGroupQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCapacityGroup): Boolean {
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

    fun apply(items: MutableList<TieredCapacityGroup>): MutableList<TieredCapacityGroup> {
        val matched = filter(items) { candidate: TieredCapacityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCapacityGroups(store: TieredCapacityGroupStore, query: TieredCapacityGroupQuery): MutableList<TieredCapacityGroup> {
    return query.apply(store.list())
}
