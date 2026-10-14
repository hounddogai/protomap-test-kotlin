package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps manual sortation rollups by code. */
interface ManualSortationRollupStore {
    fun find(code: String): ManualSortationRollup?
    fun save(item: ManualSortationRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualSortationRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualSortationRollupStore(
    var items: MutableMap<String, ManualSortationRollup>,
    var order: MutableList<String>,
) : ManualSortationRollupStore {
    override fun find(code: String): ManualSortationRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualSortationRollup) {
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

    override fun list(): MutableList<ManualSortationRollup> {
        val out = mutableListOf<ManualSortationRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualSortationRollupStore(): ManualSortationRollupStore {
    return MemoryManualSortationRollupStore(
        mutableMapOf<String, ManualSortationRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualSortationRollupStore(
    var inner: ManualSortationRollupStore,
    var cache: TtlCache<ManualSortationRollup>,
    var hits: Int,
    var misses: Int,
) : ManualSortationRollupStore {
    override fun find(code: String): ManualSortationRollup? {
        val hit = (cache.get(code) as ManualSortationRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualSortationRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualSortationRollup> = inner.list()
}

fun newCachedManualSortationRollupStore(inner: ManualSortationRollupStore, ttl: Int, clock: Clock): ManualSortationRollupStore {
    return CachedManualSortationRollupStore(
        inner = inner,
        cache = newTtlCache<ManualSortationRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual sortation rollups. Empty fields match all. */
data class ManualSortationRollupQuery(
    var lane: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualSortationRollup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ManualSortationRollup>): MutableList<ManualSortationRollup> {
        val matched = filter(items) { candidate: ManualSortationRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualSortationRollups(store: ManualSortationRollupStore, query: ManualSortationRollupQuery): MutableList<ManualSortationRollup> {
    return query.apply(store.list())
}
