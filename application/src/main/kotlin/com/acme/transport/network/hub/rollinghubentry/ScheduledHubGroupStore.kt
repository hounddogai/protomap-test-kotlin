package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled hub groups by code. */
interface ScheduledHubGroupStore {
    fun find(code: String): ScheduledHubGroup?
    fun save(item: ScheduledHubGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHubGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHubGroupStore(
    var items: MutableMap<String, ScheduledHubGroup>,
    var order: MutableList<String>,
) : ScheduledHubGroupStore {
    override fun find(code: String): ScheduledHubGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHubGroup) {
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

    override fun list(): MutableList<ScheduledHubGroup> {
        val out = mutableListOf<ScheduledHubGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHubGroupStore(): ScheduledHubGroupStore {
    return MemoryScheduledHubGroupStore(mutableMapOf<String, ScheduledHubGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHubGroupStore(
    var inner: ScheduledHubGroupStore,
    var cache: TtlCache<ScheduledHubGroup>,
    var hits: Int,
    var misses: Int,
) : ScheduledHubGroupStore {
    override fun find(code: String): ScheduledHubGroup? {
        val hit = (cache.get(code) as ScheduledHubGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHubGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHubGroup> = inner.list()
}

fun newCachedScheduledHubGroupStore(inner: ScheduledHubGroupStore, ttl: Int, clock: Clock): ScheduledHubGroupStore {
    return CachedScheduledHubGroupStore(
        inner = inner,
        cache = newTtlCache<ScheduledHubGroup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hub groups. Empty fields match all. */
data class ScheduledHubGroupQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHubGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ScheduledHubGroup>): MutableList<ScheduledHubGroup> {
        val matched = filter(items) { candidate: ScheduledHubGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHubGroups(store: ScheduledHubGroupStore, query: ScheduledHubGroupQuery): MutableList<ScheduledHubGroup> {
    return query.apply(store.list())
}
