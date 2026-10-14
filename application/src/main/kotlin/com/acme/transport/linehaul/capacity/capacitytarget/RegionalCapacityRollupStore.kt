package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps regional capacity rollups by code. */
interface RegionalCapacityRollupStore {
    fun find(code: String): RegionalCapacityRollup?
    fun save(item: RegionalCapacityRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCapacityRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCapacityRollupStore(
    var items: MutableMap<String, RegionalCapacityRollup>,
    var order: MutableList<String>,
) : RegionalCapacityRollupStore {
    override fun find(code: String): RegionalCapacityRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCapacityRollup) {
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

    override fun list(): MutableList<RegionalCapacityRollup> {
        val out = mutableListOf<RegionalCapacityRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCapacityRollupStore(): RegionalCapacityRollupStore {
    return MemoryRegionalCapacityRollupStore(
        mutableMapOf<String, RegionalCapacityRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCapacityRollupStore(
    var inner: RegionalCapacityRollupStore,
    var cache: TtlCache<RegionalCapacityRollup>,
    var hits: Int,
    var misses: Int,
) : RegionalCapacityRollupStore {
    override fun find(code: String): RegionalCapacityRollup? {
        val hit = (cache.get(code) as RegionalCapacityRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCapacityRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCapacityRollup> = inner.list()
}

fun newCachedRegionalCapacityRollupStore(inner: RegionalCapacityRollupStore, ttl: Int, clock: Clock): RegionalCapacityRollupStore {
    return CachedRegionalCapacityRollupStore(
        inner = inner,
        cache = newTtlCache<RegionalCapacityRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional capacity rollups. Empty fields match all. */
data class RegionalCapacityRollupQuery(
    var unit: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCapacityRollup): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCapacityRollup>): MutableList<RegionalCapacityRollup> {
        val matched = filter(items) { candidate: RegionalCapacityRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCapacityRollups(store: RegionalCapacityRollupStore, query: RegionalCapacityRollupQuery): MutableList<RegionalCapacityRollup> {
    return query.apply(store.list())
}
