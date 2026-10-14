package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker groups by code. */
interface LockerGroupStore {
    fun find(code: String): LockerGroup?
    fun save(item: LockerGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerGroupStore(
    var items: MutableMap<String, LockerGroup>,
    var order: MutableList<String>,
) : LockerGroupStore {
    override fun find(code: String): LockerGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerGroup) {
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

    override fun list(): MutableList<LockerGroup> {
        val out = mutableListOf<LockerGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerGroupStore(): LockerGroupStore {
    return MemoryLockerGroupStore(mutableMapOf<String, LockerGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerGroupStore(
    var inner: LockerGroupStore,
    var cache: TtlCache<LockerGroup>,
    var hits: Int,
    var misses: Int,
) : LockerGroupStore {
    override fun find(code: String): LockerGroup? {
        val hit = (cache.get(code) as LockerGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerGroup> = inner.list()
}

fun newCachedLockerGroupStore(inner: LockerGroupStore, ttl: Int, clock: Clock): LockerGroupStore {
    return CachedLockerGroupStore(
        inner = inner,
        cache = newTtlCache<LockerGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker groups. Empty fields match all. */
data class LockerGroupQuery(
    var lane: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerGroup>): MutableList<LockerGroup> {
        val matched = filter(items) { candidate: LockerGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerGroups(store: LockerGroupStore, query: LockerGroupQuery): MutableList<LockerGroup> {
    return query.apply(store.list())
}
