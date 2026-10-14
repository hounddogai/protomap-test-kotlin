package com.acme.transport.terminal.traileryard.yardforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard groups by code. */
interface TrailerYardGroupStore {
    fun find(code: String): TrailerYardGroup?
    fun save(item: TrailerYardGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardGroupStore(
    var items: MutableMap<String, TrailerYardGroup>,
    var order: MutableList<String>,
) : TrailerYardGroupStore {
    override fun find(code: String): TrailerYardGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardGroup) {
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

    override fun list(): MutableList<TrailerYardGroup> {
        val out = mutableListOf<TrailerYardGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardGroupStore(): TrailerYardGroupStore {
    return MemoryTrailerYardGroupStore(mutableMapOf<String, TrailerYardGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardGroupStore(
    var inner: TrailerYardGroupStore,
    var cache: TtlCache<TrailerYardGroup>,
    var hits: Int,
    var misses: Int,
) : TrailerYardGroupStore {
    override fun find(code: String): TrailerYardGroup? {
        val hit = (cache.get(code) as TrailerYardGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardGroup> = inner.list()
}

fun newCachedTrailerYardGroupStore(inner: TrailerYardGroupStore, ttl: Int, clock: Clock): TrailerYardGroupStore {
    return CachedTrailerYardGroupStore(
        inner = inner,
        cache = newTtlCache<TrailerYardGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard groups. Empty fields match all. */
data class TrailerYardGroupQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardGroup>): MutableList<TrailerYardGroup> {
        val matched = filter(items) { candidate: TrailerYardGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardGroups(store: TrailerYardGroupStore, query: TrailerYardGroupQuery): MutableList<TrailerYardGroup> {
    return query.apply(store.list())
}
