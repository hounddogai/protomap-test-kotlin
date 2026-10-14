package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered dwell groups by code. */
interface TieredDwellGroupStore {
    fun find(code: String): TieredDwellGroup?
    fun save(item: TieredDwellGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDwellGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDwellGroupStore(
    var items: MutableMap<String, TieredDwellGroup>,
    var order: MutableList<String>,
) : TieredDwellGroupStore {
    override fun find(code: String): TieredDwellGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDwellGroup) {
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

    override fun list(): MutableList<TieredDwellGroup> {
        val out = mutableListOf<TieredDwellGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDwellGroupStore(): TieredDwellGroupStore {
    return MemoryTieredDwellGroupStore(mutableMapOf<String, TieredDwellGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDwellGroupStore(
    var inner: TieredDwellGroupStore,
    var cache: TtlCache<TieredDwellGroup>,
    var hits: Int,
    var misses: Int,
) : TieredDwellGroupStore {
    override fun find(code: String): TieredDwellGroup? {
        val hit = (cache.get(code) as TieredDwellGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDwellGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDwellGroup> = inner.list()
}

fun newCachedTieredDwellGroupStore(inner: TieredDwellGroupStore, ttl: Int, clock: Clock): TieredDwellGroupStore {
    return CachedTieredDwellGroupStore(
        inner = inner,
        cache = newTtlCache<TieredDwellGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dwell groups. Empty fields match all. */
data class TieredDwellGroupQuery(
    var country: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDwellGroup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDwellGroup>): MutableList<TieredDwellGroup> {
        val matched = filter(items) { candidate: TieredDwellGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDwellGroups(store: TieredDwellGroupStore, query: TieredDwellGroupQuery): MutableList<TieredDwellGroup> {
    return query.apply(store.list())
}
