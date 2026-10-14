package com.acme.transport.crewing.roster.proposal

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending roster rollups by code. */
interface PendingRosterRollupStore {
    fun find(code: String): PendingRosterRollup?
    fun save(item: PendingRosterRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRosterRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRosterRollupStore(
    var items: MutableMap<String, PendingRosterRollup>,
    var order: MutableList<String>,
) : PendingRosterRollupStore {
    override fun find(code: String): PendingRosterRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRosterRollup) {
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

    override fun list(): MutableList<PendingRosterRollup> {
        val out = mutableListOf<PendingRosterRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRosterRollupStore(): PendingRosterRollupStore {
    return MemoryPendingRosterRollupStore(mutableMapOf<String, PendingRosterRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRosterRollupStore(
    var inner: PendingRosterRollupStore,
    var cache: TtlCache<PendingRosterRollup>,
    var hits: Int,
    var misses: Int,
) : PendingRosterRollupStore {
    override fun find(code: String): PendingRosterRollup? {
        val hit = (cache.get(code) as PendingRosterRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRosterRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRosterRollup> = inner.list()
}

fun newCachedPendingRosterRollupStore(inner: PendingRosterRollupStore, ttl: Int, clock: Clock): PendingRosterRollupStore {
    return CachedPendingRosterRollupStore(
        inner = inner,
        cache = newTtlCache<PendingRosterRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending roster rollups. Empty fields match all. */
data class PendingRosterRollupQuery(
    var depot: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRosterRollup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRosterRollup>): MutableList<PendingRosterRollup> {
        val matched = filter(items) { candidate: PendingRosterRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRosterRollups(store: PendingRosterRollupStore, query: PendingRosterRollupQuery): MutableList<PendingRosterRollup> {
    return query.apply(store.list())
}
