package com.acme.transport.customs.clearance.clearanceestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending clearance overrides by code. */
interface PendingClearanceOverrideStore {
    fun find(code: String): PendingClearanceOverride?
    fun save(item: PendingClearanceOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingClearanceOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingClearanceOverrideStore(
    var items: MutableMap<String, PendingClearanceOverride>,
    var order: MutableList<String>,
) : PendingClearanceOverrideStore {
    override fun find(code: String): PendingClearanceOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingClearanceOverride) {
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

    override fun list(): MutableList<PendingClearanceOverride> {
        val out = mutableListOf<PendingClearanceOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingClearanceOverrideStore(): PendingClearanceOverrideStore {
    return MemoryPendingClearanceOverrideStore(
        mutableMapOf<String, PendingClearanceOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingClearanceOverrideStore(
    var inner: PendingClearanceOverrideStore,
    var cache: TtlCache<PendingClearanceOverride>,
    var hits: Int,
    var misses: Int,
) : PendingClearanceOverrideStore {
    override fun find(code: String): PendingClearanceOverride? {
        val hit = (cache.get(code) as PendingClearanceOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingClearanceOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingClearanceOverride> = inner.list()
}

fun newCachedPendingClearanceOverrideStore(inner: PendingClearanceOverrideStore, ttl: Int, clock: Clock): PendingClearanceOverrideStore {
    return CachedPendingClearanceOverrideStore(
        inner = inner,
        cache = newTtlCache<PendingClearanceOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending clearance overrides. Empty fields match all. */
data class PendingClearanceOverrideQuery(
    var lane: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingClearanceOverride): Boolean {
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

    fun apply(items: MutableList<PendingClearanceOverride>): MutableList<PendingClearanceOverride> {
        val matched = filter(items) { candidate: PendingClearanceOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingClearanceOverrides(store: PendingClearanceOverrideStore, query: PendingClearanceOverrideQuery): MutableList<PendingClearanceOverride> {
    return query.apply(store.list())
}
