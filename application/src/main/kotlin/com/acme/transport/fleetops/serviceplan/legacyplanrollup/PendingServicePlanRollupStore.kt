package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending service plan rollups by code. */
interface PendingServicePlanRollupStore {
    fun find(code: String): PendingServicePlanRollup?
    fun save(item: PendingServicePlanRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingServicePlanRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingServicePlanRollupStore(
    var items: MutableMap<String, PendingServicePlanRollup>,
    var order: MutableList<String>,
) : PendingServicePlanRollupStore {
    override fun find(code: String): PendingServicePlanRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingServicePlanRollup) {
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

    override fun list(): MutableList<PendingServicePlanRollup> {
        val out = mutableListOf<PendingServicePlanRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingServicePlanRollupStore(): PendingServicePlanRollupStore {
    return MemoryPendingServicePlanRollupStore(
        mutableMapOf<String, PendingServicePlanRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingServicePlanRollupStore(
    var inner: PendingServicePlanRollupStore,
    var cache: TtlCache<PendingServicePlanRollup>,
    var hits: Int,
    var misses: Int,
) : PendingServicePlanRollupStore {
    override fun find(code: String): PendingServicePlanRollup? {
        val hit = (cache.get(code) as PendingServicePlanRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingServicePlanRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingServicePlanRollup> = inner.list()
}

fun newCachedPendingServicePlanRollupStore(inner: PendingServicePlanRollupStore, ttl: Int, clock: Clock): PendingServicePlanRollupStore {
    return CachedPendingServicePlanRollupStore(
        inner = inner,
        cache = newTtlCache<PendingServicePlanRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending service plan rollups. Empty fields match all. */
data class PendingServicePlanRollupQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingServicePlanRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingServicePlanRollup>): MutableList<PendingServicePlanRollup> {
        val matched = filter(items) { candidate: PendingServicePlanRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingServicePlanRollups(store: PendingServicePlanRollupStore, query: PendingServicePlanRollupQuery): MutableList<PendingServicePlanRollup> {
    return query.apply(store.list())
}
