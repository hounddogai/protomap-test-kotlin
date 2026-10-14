package com.acme.transport.linehaul.backhaul.backhaulallocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy backhaul allocations by code. */
interface LegacyBackhaulAllocationStore {
    fun find(code: String): LegacyBackhaulAllocation?
    fun save(item: LegacyBackhaulAllocation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBackhaulAllocation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBackhaulAllocationStore(
    var items: MutableMap<String, LegacyBackhaulAllocation>,
    var order: MutableList<String>,
) : LegacyBackhaulAllocationStore {
    override fun find(code: String): LegacyBackhaulAllocation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBackhaulAllocation) {
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

    override fun list(): MutableList<LegacyBackhaulAllocation> {
        val out = mutableListOf<LegacyBackhaulAllocation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBackhaulAllocationStore(): LegacyBackhaulAllocationStore {
    return MemoryLegacyBackhaulAllocationStore(
        mutableMapOf<String, LegacyBackhaulAllocation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBackhaulAllocationStore(
    var inner: LegacyBackhaulAllocationStore,
    var cache: TtlCache<LegacyBackhaulAllocation>,
    var hits: Int,
    var misses: Int,
) : LegacyBackhaulAllocationStore {
    override fun find(code: String): LegacyBackhaulAllocation? {
        val hit = (cache.get(code) as LegacyBackhaulAllocation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBackhaulAllocation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBackhaulAllocation> = inner.list()
}

fun newCachedLegacyBackhaulAllocationStore(inner: LegacyBackhaulAllocationStore, ttl: Int, clock: Clock): LegacyBackhaulAllocationStore {
    return CachedLegacyBackhaulAllocationStore(
        inner = inner,
        cache = newTtlCache<LegacyBackhaulAllocation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy backhaul allocations. Empty fields match all. */
data class LegacyBackhaulAllocationQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBackhaulAllocation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBackhaulAllocation>): MutableList<LegacyBackhaulAllocation> {
        val matched = filter(items) { candidate: LegacyBackhaulAllocation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBackhaulAllocations(store: LegacyBackhaulAllocationStore, query: LegacyBackhaulAllocationQuery): MutableList<LegacyBackhaulAllocation> {
    return query.apply(store.list())
}
