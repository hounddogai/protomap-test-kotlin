package com.acme.transport.customs.clearance.rollup

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional clearance rollups by code. */
interface RegionalClearanceRollupStore {
    fun find(code: String): RegionalClearanceRollup?
    fun save(item: RegionalClearanceRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalClearanceRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalClearanceRollupStore(
    var items: MutableMap<String, RegionalClearanceRollup>,
    var order: MutableList<String>,
) : RegionalClearanceRollupStore {
    override fun find(code: String): RegionalClearanceRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalClearanceRollup) {
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

    override fun list(): MutableList<RegionalClearanceRollup> {
        val out = mutableListOf<RegionalClearanceRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalClearanceRollupStore(): RegionalClearanceRollupStore {
    return MemoryRegionalClearanceRollupStore(
        mutableMapOf<String, RegionalClearanceRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalClearanceRollupStore(
    var inner: RegionalClearanceRollupStore,
    var cache: TtlCache<RegionalClearanceRollup>,
    var hits: Int,
    var misses: Int,
) : RegionalClearanceRollupStore {
    override fun find(code: String): RegionalClearanceRollup? {
        val hit = (cache.get(code) as RegionalClearanceRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalClearanceRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalClearanceRollup> = inner.list()
}

fun newCachedRegionalClearanceRollupStore(inner: RegionalClearanceRollupStore, ttl: Int, clock: Clock): RegionalClearanceRollupStore {
    return CachedRegionalClearanceRollupStore(
        inner = inner,
        cache = newTtlCache<RegionalClearanceRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional clearance rollups. Empty fields match all. */
data class RegionalClearanceRollupQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalClearanceRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalClearanceRollup>): MutableList<RegionalClearanceRollup> {
        val matched = filter(items) { candidate: RegionalClearanceRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalClearanceRollups(store: RegionalClearanceRollupStore, query: RegionalClearanceRollupQuery): MutableList<RegionalClearanceRollup> {
    return query.apply(store.list())
}
